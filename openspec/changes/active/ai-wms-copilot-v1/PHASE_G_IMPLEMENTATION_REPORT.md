# PHASE G IMPLEMENTATION REPORT — AI Draft Assistant

**Branch:** `feature/ai-wms-copilot-v1`  
**Phase F Commit:** `34873835f26672a8175a370c45285fbee1dd2c8f`  
**Date:** 2026-09-14  
**Commit:** NO (awaiting review)

---

## Summary

Phase G delivers NL → L2 `prepare_*_draft` → Draft Card → explicit UI confirm → L3 Facade create. Natural-language “好的/确认” cannot create WMS orders. Drafts are tenant+user bound in Redis (TTL 60 min).

---

## Delivered

### Backend

| Area | Detail |
|------|--------|
| Model | `AiDraft` / `DraftType` / `DraftStatus` / `DraftValidationResult` |
| Store | `RedisAiDraftStore` key `ai:wms:draft:{tenant}:{user}:{draftId}`; `InMemoryAiDraftStore` for unit tests only |
| Service | `AiDraftService` create / cancel / confirm (CAS + lock); failure → **OPEN** + `lastConfirmError` |
| L0 | `search_customer` / `get_customer` / `search_supplier` / `get_supplier` |
| L2 | `prepare_inbound_draft` / `prepare_outbound_draft` / `prepare_stocktake_draft` |
| L3 | `create_*_order` beans exist but **skipped** from LLM ToolRegistry |
| Confirm API | `POST /api/auth/ai/drafts/{draftId}/confirm` (confirmToken only) |
| Cancel API | `POST /api/auth/ai/drafts/{draftId}/cancel` |
| Create path | `AiDraftOrderCreator` → `CkInboundFacade` / `CkOutboundFacade.createProductionSaleOutBound` / `CkStockFacade.createStockTake` |
| Orchestrator | Short-circuit `DRAFT` / `NEED_CLARIFICATION` from prepare tools |
| Prompt | `v1.3.0-phase-g` — NL confirm blocked; prepare_* only for drafts |

### Frontend

| Area | Detail |
|------|--------|
| Card | `AiDraftCard.vue` — fields, items, warnings, 修改/取消/确认创建 |
| UX | Second confirm dialog; button loading/disabled |
| API | `confirmDraft` / `cancelDraft` in `src/api/ai.js` |
| Wire | Copilot DRAFT message type + patch after confirm/cancel |

### Tests

`AiDraftPhaseGTest` covers outbound clarifications, confirm once, duplicate, cancel, expire, other user/tenant, permission, create-failure keeps OPEN; inbound draft+confirm+dup; stocktake scope clarification + confirm.

### Reality checklist

Updated with RT-G01–RT-G10 (`NOT_TESTED`).

---

## Spec Amendments / Decisions

1. **Confirm failure recovery:** Draft remains **OPEN** (with `lastConfirmError` + rotated `confirmToken`). Documented here; aligns with Frozen Spec preference.
2. **Outbound V1 subtype:** Sale outbound via `createProductionSaleOutBound` only.
3. **Draft TTL:** 60 minutes (`ai.copilot.draft.ttl-minutes`).
4. **Stocktake:** Never default `takeScope=1` (full warehouse); require product/shelf/batch.
5. **Inbound shelf:** Auto-pick first enabled shelf + WARN (must show on card).
6. **Outbound inventory:** Uses existing `batches()` allocation; does not invent available-qty formulas; insufficient allocation → WARN/BLOCK.
7. **MySQL draft table:** Not created (Redis preferred).
8. **Permission codes:** `ck:inbound:create` / `ck:outbound:create` / `ck:stocktake:create` (interim empty-permissions still allow — Phase H).

---

## Known Gaps

1. Real DB / DeepSeek / Browser E2E / real order creation: **NOT_TESTED**.
2. Inbound/outbound create return `Boolean` — `businessOrderId` may be null; FE uses orderNo + allowlist when id present.
3. Outbound auto batch/shelf allocation may BLOCK when inventory layout incomplete — user must clarify in traditional UI or re-ask.
4. Draft Edit V1 = cancel/re-ask via input prefill (no inline form editor).
5. Fine-grained create permission enforcement awaits Phase H permission map.
6. Confirm CAS is process-local synchronized + status check; multi-instance Redis needs stronger Lua/CAS in later hardening if multi-node.

---

## Build Gate

| Gate | Result |
|------|--------|
| `mvn clean compile` | PASS |
| `mvn test` | PASS |
| `npm run build` | PASS |

---

## Phase H

Not started. Ready after review + commit of Phase G.
