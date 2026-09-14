# Design: AI WMS Copilot V1

## 1. Architecture

```text
                    Vue3
                      │
                      ▼
              AI Copilot UI
                      │
                      ▼
             AiCopilotController   (/api/auth/ai/**)
                      │
                      ▼
           WarehouseAiOrchestrator
                      │
              ┌───────┴────────┐
              │                │
              ▼                ▼
          LlmClient       ToolRegistry
          (DeepSeek)      ToolExecutor
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
          ▼                    ▼                    ▼
      Query Tools         Analysis Tools         Draft Tools
          │                    │                    │
          └────────────────────┼────────────────────┘
                               ▼
                     Existing Ck* Facade
                               │
                     Existing Service
                               │
                     Mapper / MySQL / Redis
```

**Forbidden paths:** LLM → Mapper | LLM → SQL | LLM → InventoryHolder mutation | LLM → approveOk

---

## 2. Java package (adapt to monolith)

Project root package: `com.example`. Prefer logical packages (no new Maven module in V1):

```text
com.example.ai
├── controller          AiCopilotController
├── application         WarehouseAiOrchestrator
├── tool
│   ├── ToolRegistry / ToolDefinition / ToolExecutor / ToolResult / ToolRiskLevel
│   ├── product / warehouse / inventory / inbound / outbound / stocktake / analysis
│   └── support         CustomerTool, SupplierTool (entity resolution)
├── conversation        ConversationService, MessageStore
├── prompt              SystemPromptFactory
├── permission          AiToolPermissionGuard
├── audit               AiToolAuditService
├── draft               AiDraftService, AiDraft
└── llm                 LlmClient, DeepSeekLlmClient (wrap DeepSeekUtils)
```

Existing keep:

- `com.example.utils.DeepSeekUtils` / `DeepSeekConfig` — wrap, do not delete  
- `com.example.Facade.Ck*` — call from tools only  
- `com.example.filter.UserUtil` — CurrentUser / tenantId  

---

## 3. Tool architecture

### 3.1 Contract fields

Every tool registers:

| Field | Meaning |
|-------|---------|
| `name` | Stable id e.g. `get_inventory` |
| `description` | For LLM tool-calling |
| `riskLevel` | L0 / L1 / L2 / L3 (L4 not registered) |
| `permission` | Declared code e.g. `ck:inventory:list` |
| `inputSchema` | JSON schema |
| `outputSchema` | JSON schema |
| `timeoutMs` | Default 3000 for L0 |
| `audit` | true |

### 3.2 Risk levels

| Level | Behavior |
|-------|----------|
| L0 READ | Auto-run after permission check |
| L1 ANALYSIS | Auto-run; may call multiple Facades |
| L2 DRAFT | Creates `AI_DRAFT` only; no business order |
| L3 CONFIRM_REQUIRED | Runs only with `confirmToken` from UI Confirm Action |
| L4 HIGH_RISK | **Not registered in V1** (approve, stock mutation, unlock, adjust) |

### 3.3 Execution pipeline

```text
Orchestrator
  → resolve CurrentUser (reject if missing)
  → AiToolPermissionGuard
  → if L3: require confirmToken bound to draftId+userId+tenantId
  → ToolExecutor.invoke → Facade
  → AiToolAuditService.write
  → return ToolResult to LLM / frontend cards
```

Tenant: **never** from tool args supplied by the model. Always `UserUtil.getCurrentUser().getTenantId()`.

---

## 4. LLM

```text
interface LlmClient {
  ChatResult chat(ChatRequest req); // messages, tools, temperature
}
```

V1 implementation: `DeepSeekLlmClient` wrapping `DeepSeekUtils` / WebClient from `DeepSeekConfig`.

System prompt MUST include:

- Use only registered tools  
- Never invent inventory / orders  
- Never claim other-tenant data  
- Never suggest calling approve / inventory mutate tools  
- Prefer clarifying questions (`NEED_CLARIFICATION`) over guessing entities  

---

## 5. Conversation

Minimal multi-turn:

| Field | Notes |
|-------|-------|
| conversationId | UUID |
| messageId | UUID |
| userId / tenantId | from CurrentUser |
| role | user / assistant / tool |
| content | text or structured payload |
| toolCalls | name, args, resultSummary |
| createdAt | |

Storage V1: Redis preferred (TTL e.g. 24h) to avoid mandatory new MySQL tables. If Redis unavailable in env, Amendment may allow MySQL `ai_conversation` later.

No long-term memory / cross-session personalization in V1.

---

## 6. Draft + Confirm

### Draft

```text
AI_DRAFT
  draftId
  draftType: INBOUND | OUTBOUND | STOCKTAKE
  tenantId / userId
  parsedInput
  resolvedEntities   # ids after search_* tools
  payload            # mirrors InboundCreateReq / OutboundCreate* / StockTakeCreateReq shape
  warnings[]
  validationResult
  expireAt
  status: OPEN | CONFIRMED | EXPIRED | CANCELLED
```

L2 tools (`prepare_*_draft`) write draft store only.

### Confirm

Frontend shows Draft Card → buttons **取消 / 编辑 / 确认创建**.

Only **确认创建** calls API:

```text
POST /api/auth/ai/drafts/{draftId}/confirm
```

Server issues one-time `confirmToken` (or confirm endpoint itself executes L3) bound to draftId+user+tenant, then:

```text
create_inbound_order | create_outbound_order | create_stocktake_order
```

Natural language “好的 / 确认” **must not** trigger L3.

---

## 7. Entity resolution

User text is never trusted as id:

```text
"杭州仓" → search_warehouse → 0/1/N
  0 → NOT_FOUND / NEED_CLARIFICATION
  1 → auto-bind warehouseId (document in response)
  N → NEED_CLARIFICATION with options
```

Same for product/SKU, customer, supplier.

**Auto-complete rule (frozen):** if exactly one enabled warehouse for tenant and user did not specify warehouse, tool may bind that warehouse and must list it in Draft warnings as “已自动选择唯一仓库：xxx”.

---

## 8. Response schema (API → Frontend)

```json
{
  "type": "TEXT | TOOL_RESULT | DRAFT | ERROR | PERMISSION_DENIED | NEED_CLARIFICATION",
  "message": "human readable",
  "conversationId": "...",
  "toolCalls": [],
  "cards": [],
  "actions": [],
  "draft": null
}
```

Errors classify: `BUSINESS_ERROR | PERMISSION_ERROR | VALIDATION_ERROR | NOT_FOUND | TIMEOUT | SYSTEM_ERROR`.  
LLM must not rewrite `SYSTEM_ERROR` into business facts.

---

## 9. Tenant & permission

### Tenant

| Source | Allowed |
|--------|---------|
| JWT / UserUtil | YES |
| AI tool argument `tenantId` | **IGNORED / REJECTED** |

### RBAC (V1 decision — freeze)

**Fact:** Current WMS Controllers rely on authenticated JWT + Facade tenant filters; `@PreAuthorize` not wired; `ck_menu`/`ck_role` cleanup unfinished.

**Frozen interim:**

1. Every tool declares `permission` string.  
2. `AiToolPermissionGuard` checks: user authenticated + tenant present.  
3. If role-permission mapping becomes available (Account.role or menu permissions), enforce deny when missing.  
4. Until mapping exists: **authenticated tenant users may call L0/L1 tools they could already reach via REST** — documented as EXISTING_MODEL parity, not elevation.  
5. Phase H must ship real deny-by-permission for AI tools (cannot permanently stay open).  

AI MUST still return `PERMISSION_DENIED` when Guard denies.

---

## 10. Audit

Reuse pattern of `@LogOperation` / `ck_operation_log` where practical; plus dedicated AI audit record (Redis or table — prefer Redis/json log first):

```text
conversationId, userId, tenantId, userQuestion, model,
toolName, toolInput(redacted), toolResultSummary, latencyMs,
success, riskLevel, confirmedByUser, inputToken, outputToken, toolCallCount
```

Do not log full secrets or entire inventory dumps.

---

## 11. Frontend

| Entry | V1 |
|-------|----|
| Global Copilot | Right Drawer — chat, cards, draft confirm |
| AI Daily Workspace | Enhance `CkIndex` / dashboard with summary from `get_daily_warehouse_summary` + `get_my_pending_tasks` |
| Context actions | Optional: outbound detail “AI 分析该单”; inventory “AI 分析” — can land after global + dashboard |

API base: `/api/auth/ai/**` (authenticated).

---

## 12. Data flow examples

### Query inventory

```text
User: A001还有多少库存？
→ LLM: search_product / get_inventory
→ CkInventoryFacade.pageList + productInventoryDetail (compose)
→ Card with quantity / lockedQuantity as returned by system
→ TEXT summary (no invented numbers)
```

### Outbound draft

```text
User: 杭州仓给ABC客户出 A001×100
→ search_warehouse, search_customer, search_product, optional get_inventory warning
→ prepare_outbound_draft → DRAFT card
→ User clicks 确认创建
→ create_outbound_order → existing createProductionSaleOutBound / type-specific Facade
```

### Explain failure

```text
User: 为什么 CK… 不能出库？
→ get_outbound_order + inventory/lock/checkBatchAllocation compose
→ explain_outbound_failure (L1)
→ structured reasons + suggested next steps (no auto-fix)
```

---

## 13. Performance & cost

| Target | Value |
|--------|-------|
| L0 tool P95 | < 3s |
| End-to-end query | 3–8s typical |
| Max tool calls / turn | soft cap e.g. 6 (configurable) |

Log token usage; no billing system change in V1.

---

## 14. Error / clarification

Orchestrator maps Facade `ValidationException` → `BUSINESS_ERROR` / `VALIDATION_ERROR`.  
Missing entities → `NEED_CLARIFICATION` with selectable options in `cards`.
