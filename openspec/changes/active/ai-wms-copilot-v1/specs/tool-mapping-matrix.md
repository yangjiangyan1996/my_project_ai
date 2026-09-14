# Spec: Tool Mapping Matrix (HEAD)

> Source: live Controllers/Facades under `my-project-backend`  
> Reuse: **YES** | **PARTIAL** | **NO**  
> Permission: backend today = JWT authenticated; declared codes are **targets** for Guard/Phase H

Status enums (`CkInOutboundEnums.InOutBoundStatus`):  
`0` WaitSubmit, `1` WaitAudit, `2` AuditPass, `3` Complete, `4` Reject, `9` Cancel.

---

## Matrix

| Tool | Existing Capability | Declared permission | Reuse | Gap |
|------|---------------------|---------------------|-------|-----|
| `search_product` | `GET /api/auth/product/search` → `CKProductFacade.productSimpleList`; also `POST .../product/pageList` | `ck:product:list` | YES | — |
| `get_product` | `GET /api/auth/product/detail` → `CKProductFacade.detail` | `ck:product:list` | YES | — |
| `search_warehouse` | `POST /api/auth/warehouse/pageList` → `CkWarehouseFacade`; `GET .../listEnable` | `ck:warehouse:list` | YES | no dedicated keyword API |
| `get_warehouse` | Facade internal `getById` on update; no public get-by-id | `ck:warehouse:list` | PARTIAL | adapter: pageList/list filter by id or thin Facade expose |
| `get_inventory` | `GET .../inventory/productInventoryDetail` + `POST .../inventory/pageList` | `ck:inventory:list` | PARTIAL | compose for qty/locked by warehouse |
| `get_inventory_detail` | pageList + `GET .../inventory/batches` | `ck:inventory:list` | YES | multi-call compose |
| `search_inventory` | `POST .../inventory/pageList` | `ck:inventory:list` | YES | — |
| `get_inbound_order` | `GET .../inbound/detail` → `CkInboundFacade.detail` | `ck:inbound:list` | YES | production detail variant |
| `list_inbound_orders` | `POST .../inbound/pageList` | `ck:inbound:list` | YES | — |
| `list_pending_inbounds` | pageList `status=1` + `countsOfManagePage` | `ck:inbound:list` | YES | no dedicated API |
| `prepare_inbound_draft` | — | `ck:inbound:create` | NO | new Draft layer |
| `create_inbound_order` | `POST .../inbound/create` → `CkInboundFacade.create` (`InboundCreateReq`) | `ck:inbound:create` | YES | only after confirm; prefer status draft/WaitSubmit |
| `get_outbound_order` | `GET .../outbound/detail` → `CkOutboundFacade.detail` | `ck:outbound:list` | YES | type-specific details |
| `list_outbound_orders` | `POST .../outbound/pageList` | `ck:outbound:list` | YES | — |
| `list_pending_outbounds` | pageList `status=1` + counts | `ck:outbound:list` | YES | — |
| `explain_outbound_failure` | compose detail + `checkBatchAllocation` + lock queries | `ck:outbound:list` | PARTIAL | new L1 composer tool |
| `prepare_outbound_draft` | — | `ck:outbound:create` | NO | Draft layer |
| `create_outbound_order` | `createProductionSaleOutBound` / `createProductionPickingOutBound` | `ck:outbound:create` | PARTIAL | type-specific; no single `/create` |
| `get_stocktake_order` | `GET .../stock/stockDetail` → `CkStockFacade.stockDetail` | `ck:stocktake:list` | YES | — |
| `list_stocktake_orders` | `POST .../stock/pageList` | `ck:stocktake:list` | YES | — |
| `list_pending_stocktakes` | pageList + approval/execute status filters | `ck:stocktake:list` | PARTIAL | define pending = 待审 or 待盘 in tool docs |
| `prepare_stocktake_draft` | — | `ck:stocktake:create` | NO | Draft layer |
| `create_stocktake_order` | `POST .../stock/createStockTake` → `CkStockFacade.createStockTake` | `ck:stocktake:create` | YES | after confirm |
| `get_daily_warehouse_summary` | `GET .../inventory/countsOfIndexPage` | `ck:dashboard:read` | PARTIAL | compose + optional inbound/outbound counts |
| `get_my_pending_tasks` | FE expected `/approval/pendingTasks` **missing**; use counts/pageList compose | `ck:dashboard:read` | NO→compose | **do not restore community approval module**; compose WMS pending |
| `analyze_inventory_risk` | `POST .../inventory/alerts`, `alertStats`, `lowProductCountChat` | `ck:inventory:list` | YES | rule-based alerts only |
| `search_customer` | `POST .../customer/pageList`, `listEnable` | `ck:customer:list` | YES | entity resolution |
| `search_supplier` | `POST .../supplier/pageList`, `listEnable` | `ck:supplier:list` | YES | entity resolution |

---

## Inventory fields (do not reinterpret)

From list/page responses: `quantity`, `lockedQuantity`, `availableQuantity` (when Facade sets it).  
Product history detail may expose `currentStock` without locked — tool must label source.

---

## Mapping completeness

| Category | Status |
|----------|--------|
| L0 query tools | COMPLETE enough (PARTIAL adapters documented) |
| L1 analysis | COMPLETE with compose gaps documented |
| L2/L3 draft/create | PARTIAL — Draft store new; create Facades exist |
| Overall Tool Mapping | **COMPLETE** for V1 planning (gaps are implementation work, not unknown unknowns) |

---

## Explicit non-mapping (forbidden)

| Capability | Status |
|------------|--------|
| `approveOk` inbound/outbound | L4 — not mapped as AI tool |
| `InventoryHolder.updateAdd/Sub` | L4 — not mapped |
| `CkInventoryLockService` lock/unlock | L4 — not mapped |
| Adjust execute / stock variance apply | L4 — not mapped |
