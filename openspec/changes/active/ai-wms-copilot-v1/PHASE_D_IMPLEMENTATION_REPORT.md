# Phase D Implementation Report — Tool Calling Orchestrator

> Branch: `feature/ai-wms-copilot-v1`  
> Phase C commit: `691adab`  
> Phase D: **uncommitted** (await review)  
> Do **not** enter Phase E until approved

---

## Scope delivered (this turn)

Implements **D5 Orchestrator tool-calling loop** + **D6 prompt** + bounded InMemory conversation (D7 Redis deferred).

**Explicitly deferred** (Frozen `tasks.md` D1–D4 L1 analysis tools):

| ID | Tool | Reason |
|----|------|--------|
| D1 | `get_daily_warehouse_summary` | Overlaps Daily Workspace (Phase F); this turn = loop only |
| D2 | `get_my_pending_tasks` | Same |
| D3 | `analyze_inventory_risk` | L1 analysis follow-up |
| D4 | `explain_outbound_failure` | L1 analysis follow-up |
| D7 | Redis ConversationStore | InMemory + TTL/bounds kept; Redis later |

No Spec Deviation to Frozen product Scope — analysis tools remain planned; orchestrator is the Phase D critical path.

---

## Tool Calling Loop

```text
User Message
 → WarehouseAiOrchestrator (explicit for-loop)
 → LlmClient (+ tools via LlmToolSchemaAdapter)
 → if tool_calls: ToolCallGuard → ToolExecutor → compress → append tool msg → continue
 → if final text: CopilotResponse TEXT
 → max rounds = maxToolCalls + 1
```

No recursive `chat() → chat()`.

Config (`application.yml`):

| Key | Default |
|-----|---------|
| `ai.copilot.max-tool-calls` | 6 |
| `ai.copilot.max-same-tool-calls` | 2 |
| `ai.copilot.conversation.max-messages` | 40 |
| `ai.copilot.conversation.max-sessions` | 500 |
| `ai.copilot.conversation.ttl-hours` | 24 |

---

## Multi-step / Entity Resolution

Mock-verified path:

`search_product` → `search_warehouse` → `get_inventory` → Final

LLM selects tools (no keyword if/else routing). Entity IDs come from prior tool results in the same turn’s message list.

---

## Ambiguous Handling

If ToolResult success payload has `ambiguous=true` and `items.size()>1`:

→ immediate `NEED_CLARIFICATION` + clarification `cards`  
→ LLM is **not** asked to pick randomly

---

## Guards

| Guard | Behavior |
|-------|----------|
| Max tool calls | `ToolCallGuard.assertWithinLimit` |
| Loop fingerprint | `toolName + normalized args`; stop at `max-same-tool-calls` |
| Unknown tool | `NOT_FOUND` tool result fed back (or blocked) |
| L4 | Cannot register (Phase B); risk gate intact |
| Permission | Executor + early `PERMISSION_DENIED` response |
| Tenant | Context-only; args sanitized |

---

## LLM Adapter

- Internal: `LlmChatResult` + `LlmToolCall` + multi-role `LlmMessage`
- DeepSeek: `ChatRequest`/`DeepSeekResponse` extended for `tools` / `tool_calls` / `usage`
- `DeepSeekUtils.chatCompletions(ChatRequest)` 
- Provider DTOs stay in adapter / utils — **not** in Orchestrator

---

## Conversation

- Roles: user / assistant / tool (stored); LLM replay uses user+assistant text for multi-turn
- Tool results compressed via `ToolResultContextCompressor` (list cap 20, JSON cap 4000, strip tenant/secrets)
- InMemory bounds + TTL (Redis still deferred)

---

## Usage + Audit

- Per LLM call: input/output tokens (nullable), latency, model  
- Aggregate on `CopilotChatResponse.usage`  
- `AiAuditRecorder.recordOrchestration(...)` end-of-request summary  
- `debugToolTrace` only when `dev` profile active

---

## Tests (Mock LLM)

`WarehouseAiOrchestratorPhaseDTest` — Cases 1–12:

| Case | Result |
|------|--------|
| No tool / single / multi-step | PASS |
| Unknown tool / invalid args | PASS |
| Permission denied | PASS |
| L4 register rejected | PASS |
| Timeout → no fabricate path | PASS |
| Loop + maxToolCalls | PASS |
| Ambiguous → NEED_CLARIFICATION | PASS |
| Multi-turn prior assistant text | PASS |

Full `mvn clean test`: **PASS**

---

## Reality tests deferred (company env)

| Item | Status |
|------|--------|
| Real DeepSeek | NOT_TESTED |
| Real DB / WMS E2E | NOT_TESTED |
| Frontend E2E | NOT_TESTED |

Does **not** block CODE COMPLETE for Phase D orchestrator.

---

## Known Gaps

1. Frozen D1–D4 L1 analysis tools not implemented this turn  
2. Redis ConversationStore deferred (InMemory bounds only)  
3. Prior-turn tool messages not replayed to LLM (assistant text only) — sufficient for V1 multi-turn clarifications  
4. RBAC still interim (Phase H)  
5. `AMBIGUOUS` still success payload at Tool layer; Orchestrator maps to `NEED_CLARIFICATION`  
6. Real provider smoke not run

---

## Phase E Ready

**YES** — chat API now runs multi-step tool calling against the 15 Phase C production tools; frontend can bind to existing `/api/auth/ai/chat` + structured `CopilotChatResponse`.

---

## Commit

Phase D: **NO**
