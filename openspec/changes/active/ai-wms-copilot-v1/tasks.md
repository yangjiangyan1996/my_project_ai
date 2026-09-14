# Tasks: AI WMS Copilot V1

Implementation order. **Do not start until Spec VERDICT = FROZEN and product owner says start Phase A.**

Each task: content / files / deps / test / acceptance.

---

## Phase A — AI Foundation

| ID | Content | Files (expected) | Deps | Test | Acceptance |
|----|---------|------------------|------|------|------------|
| A1 | Package skeleton `com.example.ai.*` | new packages under backend | — | compile | packages exist |
| A2 | `LlmClient` + `DeepSeekLlmClient` wrap | `ai/llm/*`, reuse `DeepSeekUtils` | A1 | unit mock LLM | chat returns text |
| A3 | `AiCopilotController` stub health/chat | `ai/controller/AiCopilotController` | A2 | auth required 401 | `/api/auth/ai/health` |
| A4 | CurrentUser tenant injection helper | `ai/permission/AiUserContext` | A1 | unit | tenant from UserUtil only |

---

## Phase B — Tool Framework

| ID | Content | Files | Deps | Test | Acceptance |
|----|---------|-------|------|------|------------|
| B1 | `ToolDefinition`, `ToolRiskLevel`, `ToolResult`, error enums | `ai/tool/*` | A1 | unit | schema validation |
| B2 | `ToolRegistry` register/list for LLM | `ToolRegistry` | B1 | unit | L4 cannot register |
| B3 | `ToolExecutor` + timeout + audit hook | `ToolExecutor`, `AiToolAuditService` | B2 | unit | L3 blocked without confirm |
| B4 | `AiToolPermissionGuard` interim rules | `permission/*` | A4 | unit | unauthenticated denied |

---

## Phase C — WMS Query Tools (L0)

| ID | Content | Mapping | Deps | Test | Acceptance |
|----|---------|---------|------|------|------------|
| C1 | `search_product`, `get_product` | `CKProductFacade` | B3 | integration/mock | keyword → products |
| C2 | `search_warehouse`, `get_warehouse` | `CkWarehouseFacade` / list+filter | B3 | mock | resolve warehouse |
| C3 | `get_inventory`, `get_inventory_detail`, `search_inventory` | `CkInventoryFacade` | B3 | mock | returns qty+locked raw |
| C4 | inbound get/list/pending | `CkInboundFacade` | B3 | mock | status filter WaitAudit |
| C5 | outbound get/list/pending | `CkOutboundFacade` | B3 | mock | status filter |
| C6 | stocktake get/list/pending | `CkStockFacade` | B3 | mock | pending semantics documented |
| C7 | `search_customer`, `search_supplier` | Customer/Supplier Facade | B3 | mock | entity resolution |

---

## Phase D — Analysis Tools (L1) + Orchestrator

| ID | Content | Mapping | Deps | Test | Acceptance |
|----|---------|---------|------|------|------------|
| D1 | `get_daily_warehouse_summary` | `countsOfIndexPage` + compose | C* | mock | dashboard fields |
| D2 | `get_my_pending_tasks` | compose inbound/outbound/stock counts (**not** missing Approval API) | C* | mock | tenant-scoped tasks |
| D3 | `analyze_inventory_risk` | `alerts` / `alertStats` / `lowProductCountChat` | C3 | mock | rule-based only |
| D4 | `explain_outbound_failure` | order + `checkBatchAllocation` + locks | C5,C3 | mock | structured reasons |
| D5 | `WarehouseAiOrchestrator` tool-calling loop | LLM + Registry | D1–D4,B3 | unit | max tool calls |
| D6 | Prompt templates + injection rules | `ai/prompt/*` | D5 | unit | no fabricate rule present |
| D7 | Conversation Redis store | `ai/conversation/*` | D5 | unit | multi-turn sku resolve |

---

## Phase E — Frontend Copilot

| ID | Content | Files | Deps | Test | Acceptance |
|----|---------|-------|------|------|------------|
| E1 | Global Drawer Copilot UI | `my-project-frontend/src/components/ai/*` | D5 API | manual | chat + cards |
| E2 | Response renderers TEXT/TOOL_RESULT/ERROR/… | same | E1 | manual | typed responses |
| E3 | Wire layout entry “AI 仓储助手” | layout / router | E1 | manual | visible when logged in |

---

## Phase F — AI Daily Workspace

| ID | Content | Files | Deps | Test | Acceptance |
|----|---------|-------|------|------|------------|
| F1 | Dashboard call summary + pending tools | `CkIndex.vue` or welcome | D1,D2 | manual | “今天待处理” |
| F2 | Optional deep-links to WMS pages | FE | F1 | manual | click → existing routes |

---

## Phase G — Draft Tools (L2/L3)

| ID | Content | Mapping | Deps | Test | Acceptance |
|----|---------|---------|------|------|------------|
| G1 | Draft store + expire | Redis `AiDraftService` | B3 | unit | TTL expire |
| G2 | `prepare_inbound_draft` | assemble `InboundCreateReq` | C1,C2,C7 | unit | warnings |
| G3 | `prepare_outbound_draft` | assemble sale/picking req | C1–C3,C7 | unit | stock warning |
| G4 | `prepare_stocktake_draft` | assemble `StockTakeCreateReq` | C2,C6 | unit | scope fields |
| G5 | Confirm API + UI Confirm Action | FE Draft Card + BE confirm | G2–G4 | e2e manual | NL cannot create |
| G6 | `create_*_order` L3 → Facade create | Inbound/Outbound/Stock create | G5 | e2e | real order after confirm |

**Outbound create note:** call existing `createProductionSaleOutBound` / `createProductionPickingOutBound` by draft subtype; no invent generic `/create` unless Amendment.

---

## Phase H — Security + Audit

| ID | Content | Deps | Test | Acceptance |
|----|---------|------|------|------------|
| H1 | Harden `AiToolPermissionGuard` beyond JWT-only | B4 | unit | deny without declared perm when mapping exists |
| H2 | Audit completeness + redaction | B3 | unit | audit row per tool |
| H3 | Confirm token single-use / replay reject | G5 | unit | second confirm fails |
| H4 | Prompt regression: refuse invent stock | D6 | unit/fixture | |

---

## Phase I — Integration Tests

| ID | Content | Deps | Acceptance |
|----|---------|------|------------|
| I1 | US-01…US-10 scenario checklist | all | mapped to AC |
| I2 | Tenant isolation: user A cannot read B via tool | H | PASS |
| I3 | L4 tools absent from registry | B2 | PASS |
| I4 | Approve/inventory mutate not callable | I3 | PASS |

---

## Explicit non-tasks (refuse)

- Auto approve tools  
- InventoryHolder write tools  
- Multi-agent split  
- Forecasting / 补货  
- Restore stash AdjustTransfer/StockMove for Copilot  

---

## Suggested delivery slices

1. A+B+C+D+E → query Copilot MVP  
2. F → daily workspace  
3. G+H+I → drafts + security freeze exit
