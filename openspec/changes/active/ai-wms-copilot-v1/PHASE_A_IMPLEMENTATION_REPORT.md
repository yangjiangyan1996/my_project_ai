# Phase A Implementation Report — AI WMS Copilot V1

> Branch: `feature/ai-wms-copilot-v1`  
> Date: 2026-09-14  
> Scope: Foundation only — **no real WMS tools, no frontend, no DB schema**

---

## Implemented

| Item | Status |
|------|--------|
| Package `com.example.ai.*` | YES |
| `LlmClient` + `DeepSeekLlmClient` (wraps `DeepSeekUtils`) | YES |
| `FakeLlmClient` (test only) | YES |
| `WarehouseAiOrchestrator` | YES (chat orchestration; no Mapper/Facade) |
| `AiExecutionContext` + `AiUserContext` | YES |
| Structured `CopilotChatRequest` / `CopilotChatResponse` | YES (all Frozen types) |
| `ToolDefinition` / `ToolRiskLevel` / `ToolResult` / `ToolRequest` | YES |
| `ToolRegistry` (reject dup / L4) | YES |
| `ToolExecutor` + `ToolRiskPolicy` | YES |
| `AiPermissionChecker` (interim + Phase H TODO) | YES |
| `ConversationStore` + `InMemoryConversationStore` | YES |
| `AiAuditRecorder` + logging sink (redaction) | YES |
| `SystemPromptFactory` (versioned safety rules) | YES |
| `AiCopilotController` `GET /api/auth/ai/health`, `POST /api/auth/ai/chat` | YES |
| Typed AI exceptions | YES |
| Unit tests (18 Phase A) | PASS |

---

## Not Implemented (by design)

- Real WMS tools (`get_inventory`, inbound/outbound, …)
- Tool-calling multi-step LLM loop (Phase D)
- Draft / Confirm (Phase G)
- Redis conversation store (abstraction ready; in-memory for A)
- Frontend Copilot UI (Phase E)
- Full RBAC enforcement (Phase H)
- MySQL AI tables

---

## Architecture

```text
AiCopilotController (/api/auth/ai/**)
  → WarehouseAiOrchestrator
       → AiUserContext (JWT tenant)
       → LlmClient (DeepSeekLlmClient | Fake in tests)
       → ToolRegistry metadata (empty prod catalog in A)
       → ConversationStore
       → AiAuditRecorder
```

Tool path ready:

```text
ToolRegistry → ToolExecutor → ToolRiskPolicy + AiPermissionChecker → handler
```

**Architecture gate:** `com.example.ai` has **zero** imports of `mapper` / `Facade` / `holder`.

---

## Files Changed

- New: `my-project-backend/src/main/java/com/example/ai/**` (~33 Java files)
- New: `my-project-backend/src/test/java/com/example/ai/**` (7 test/support files)
- New: this report
- **Unchanged:** InventoryHolder, CkInbound/Outbound inventory logic, frontend, DB

---

## Tests

| Suite | Result |
|-------|--------|
| Phase A focused (18) | PASS |
| Full `mvn test` | PASS (exit 0) |
| Real DeepSeek in unit tests | **NOT used** |

Coverage highlights:

- Registry register / duplicate / unknown / L4 reject  
- L0 execute; L3 without confirm fails; L3 with token OK  
- Tenant spoof rejected  
- Orchestrator uses `LlmClient` abstraction (`FakeLlmClient`)  
- Response type contract  

---

## Spec Deviations

1. **tasks.md Phase A table lists only A1–A4**, while Phase A kickoff prompt required ToolRegistry/Executor/Risk/Orchestrator/Conversation/Audit foundations. Implemented kickoff + design foundation; noted as **early Phase B scaffolding** without real WMS tools.  
2. Conversation store is **InMemory**, not Redis (Frozen Spec allows deferred Redis; no MySQL schema added).  
3. Chat path does **not** yet invoke `ToolExecutor` in a tool-calling loop (Phase D). Registry is available to LLM metadata only.

---

## Known Gaps

1. Phase H RBAC map (`ck_role/menu` vs `Account.role`)  
2. Redis ConversationStore / Draft store  
3. Multi-turn tool-calling loop in Orchestrator  
4. Production Tool Catalog empty until Phase C  
5. Optional smoke test against real DeepSeek (manual only)

---

## Phase B Readiness

**YES** — Tool contract, registry, executor, risk policy, and permission boundary exist. Phase B can harden framework + permission guard tests; Phase C can register real Facade-backed tools.

---

## Commit / Push

**NO** — awaiting review.
