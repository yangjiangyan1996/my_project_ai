# Spec: Copilot Product (`specs/copilot.md`)

## Product

AI operation layer for multi-tenant WMS. One Warehouse Copilot + Tool Calling.

## User Stories

| ID | Story | Priority |
|----|-------|----------|
| US-01 | Ask “A001 当前库存多少？” → real WMS stock | P0 |
| US-02 | Ask “今天有多少待出库单？” → tenant pending count/list | P0 |
| US-03 | Ask “为什么 CKxxx 不能出库？” → structured explanation | P0 |
| US-04 | “帮我创建入库单…” → draft | P0 |
| US-05 | “帮我创建出库单…” → draft | P0 |
| US-06 | Only UI **确认创建** creates real order | P0 |
| US-07 | No inventory permission → cannot query via AI | P0 (Phase H enforce) |
| US-08 | Tenant A cannot read Tenant B via AI | P0 |
| US-09 | Tool/LLM failure → no fabricated business facts | P0 |
| US-10 | Home/dashboard shows today’s pending focus | P0 |

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC-01 | NL query covers product, warehouse, inventory, inbound, outbound, stocktake |
| AC-02 | All queries via Tools — no AI direct DB |
| AC-03 | Tools call existing Facade/Service |
| AC-04 | Tools inherit user identity + tenant (+ RBAC per freeze interim→H) |
| AC-05 | AI can prepare inbound/outbound/stocktake drafts |
| AC-06 | Real create requires UI confirm |
| AC-07 | No AI approve / stock mutate / variance apply / core delete |
| AC-08 | Tool calls audited |
| AC-09 | Usable Copilot UI |
| AC-10 | AI Daily Summary on home |

## Non-goals

See `freeze-baseline.md` Out-of-Scope.

## API surface (logical)

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/auth/ai/chat` | main turn |
| GET | `/api/auth/ai/conversations/{id}` | history |
| POST | `/api/auth/ai/drafts/{id}/confirm` | L3 confirm |
| POST | `/api/auth/ai/drafts/{id}/cancel` | cancel draft |
| GET | `/api/auth/ai/workspace/daily` | daily summary shortcut |
