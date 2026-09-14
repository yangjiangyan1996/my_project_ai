# Spec: Frontend Interaction

## Entries

### 1. Global Copilot (required)

- Right Drawer / Side Panel titled **AI 仓储助手**  
- Chat input, message list, streaming optional (V1 can non-stream)  
- Renders response `type`  
- Shows Tool Result Cards and Draft Cards  

### 2. AI Daily Workspace (required)

- On `CkIndex` (or main dashboard after login)  
- Section: **今天有什么需要关注？**  
- Data from `get_daily_warehouse_summary` / `get_my_pending_tasks`  
- Links into existing routes: inbound/outbound/stock/inventory  

### 3. Business context actions (optional V1)

| Page | Action |
|------|--------|
| Outbound detail | AI 分析该单 → prefill chat with orderNo |
| Inventory | AI 分析库存 |
| Inbound list | AI 帮我创建 |

May ship after global + dashboard.

## Response types to render

| type | UI |
|------|-----|
| TEXT | markdown/plain |
| TOOL_RESULT | table/cards from payload |
| DRAFT | Draft Card + Confirm buttons |
| ERROR | error banner; no fake success |
| PERMISSION_DENIED | clear denial |
| NEED_CLARIFICATION | option chips / selects |

## Confirm

Only Draft Card **确认创建** button posts confirm API.  
Chat send button must not confirm drafts.

## Auth

Copilot visible only when logged in; uses same token as WMS.

## i18n / copy

Chinese primary (match existing WMS UI).
