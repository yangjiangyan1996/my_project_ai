# Phase C — WMS Query Tool Catalog

> Branch: `feature/ai-wms-copilot-v1`  
> Phase B commit: `be9b3153b0af540408e222b19ff5d9cddc98eb23`  
> Risk: all tools **L0_READ**  
> Limits: list tools default **20**, max **50** (`items` / `total` / `hasMore`)  
> Tenant: from `AiExecutionContext` only (`tenantId` stripped from LLM args)  
> Output: AI DTOs in `AiQueryDtos` — never raw Entity dump

---

## Summary table

| Tool | Risk | Permission | Existing Service / Facade | Read Only | Status |
|------|------|------------|---------------------------|-----------|--------|
| `search_product` | L0 | `ck:product:list` | `CKProductFacade.productSimpleList` + `pageList` | YES | DONE |
| `get_product` | L0 | `ck:product:list` | `CKProductFacade.detail` | YES | DONE |
| `search_warehouse` | L0 | `ck:warehouse:list` | `CkWarehouseFacade.listOfWareHouse` (name + code) | YES | DONE |
| `get_warehouse` | L0 | `ck:warehouse:list` | `listEnable` + `CkWareHouseService.selectByTenantIdAndWareHouseId` | YES | DONE |
| `get_inventory` | L0 | `ck:inventory:list` | `CkInventoryFacade.pageList` (+ product detail for sku resolve) | YES | DONE |
| `get_inventory_detail` | L0 | `ck:inventory:list` | `CkInventoryFacade.batches` | YES | DONE |
| `get_inbound_order` | L0 | `ck:inbound:list` | `CkInboundFacade.detail` / `pageList` by orderNo | YES | DONE |
| `list_inbound_orders` | L0 | `ck:inbound:list` | `CkInboundFacade.pageList` | YES | DONE |
| `list_pending_inbounds` | L0 | `ck:inbound:list` | `pageList` status=`WaitAudit(1)` | YES | DONE |
| `get_outbound_order` | L0 | `ck:outbound:list` | `CkOutboundFacade.detail` / `pageList` | YES | DONE |
| `list_outbound_orders` | L0 | `ck:outbound:list` | `CkOutboundFacade.pageList` | YES | DONE |
| `list_pending_outbounds` | L0 | `ck:outbound:list` | `pageList` status=`WaitAudit(1)` | YES | DONE |
| `get_stocktake_order` | L0 | `ck:stocktake:list` | `CkStockFacade.stockDetail` / list resolve | YES | DONE |
| `list_stocktake_orders` | L0 | `ck:stocktake:list` | `CkStockFacade.pageList` + client post-filter | YES | DONE (GAP) |
| `list_pending_stocktakes` | L0 | `ck:stocktake:list` | pending=`ApprovalStatus.UNDER_REVIEW(1)` post-filter | YES | DONE (GAP) |

**Production Tool Count: 15**

---

## Per-tool contracts

### Product

#### `search_product`
- **Input:** `keyword` (required), `limit` optional  
- **Output:** `items[]` (`AiProductResult`), `total`, `hasMore`, `ambiguous`  
- **Tenant:** Facade via `UserInfo.tenantId` / `ProductListPageReq.tenantId`  
- **Permission:** declared `ck:product:list` (enforced when JWT permission set non-empty; else auth+tenant)  
- **Limitations:** `productSimpleList` is name-contains; SKU path uses `pageList.sku`

#### `get_product`
- **Input:** `productId`  
- **Output:** `{ product: AiProductResult }` or `NOT_FOUND`  
- **Tenant:** `CKProductFacade.detail(productId, UserInfo)`

### Warehouse

#### `search_warehouse`
- **Input:** `keyword`, `limit`  
- **Output:** list payload + `ambiguous`  
- **Tenant:** `WareHouseListPageReq.tenantId`  
- **Limitations:** no dedicated keyword API — dual name/code pageList

#### `get_warehouse`
- **Input:** `warehouseId`  
- **Output:** `{ warehouse: AiWarehouseResult }`  
- **Tenant:** `listEnable(UserInfo)` then Service `selectByTenantIdAndWareHouseId`  
- **Limitations:** no public Facade getById (**Implementation Mapping Adjustment** / PARTIAL)

### Inventory

#### `get_inventory`
- **Input:** `productId` and/or `sku`, optional `warehouseId`  
- **Output:** `items[]` (`AiInventoryResult` with warehouse qty/locked/available copied from Facade)  
- **Tenant:** `InventoryListPageReq.tenantId`  
- **Rule:** does **not** compute `quantity - lockedQuantity`  
- **Limitations:** `InventoryListPageReq` has no productId — resolve sku via product detail when only productId given

#### `get_inventory_detail`
- **Input:** `productId`, `warehouseId`  
- **Output:** batches + shelves from `CkInventoryFacade.batches`  
- **Tenant:** explicit tenantId arg to Facade (from context)

### Inbound

#### `get_inbound_order`
- **Input:** `orderId` or `orderNo`  
- **Output:** `{ order }` or ambiguous candidates or `NOT_FOUND`

#### `list_inbound_orders`
- **Input:** `status`, `warehouseId`, `orderNo`, `startDate`, `endDate`, `limit`  
- **Output:** list payload

#### `list_pending_inbounds`
- **Pending definition:** `CkInOutboundEnums.InOutBoundStatus.WaitAudit` (=1 审核中)  
- **Not** invented as `status != completed`

### Outbound

Same pattern as inbound; pending = `WaitAudit(1)`.

### Stocktake

#### `get_stocktake_order`
- **Input:** `stockTakeId` or `orderNo`  
- **NPE from missing detail → `NOT_FOUND`**

#### `list_stocktake_orders` / `list_pending_stocktakes`
- **Pending:** `CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW` (=1)  
- **CAPABILITY_GAP:** underlying `getStockPage` largely ignores filters beyond tenant; tools post-filter returned rows (total/`hasMore` less accurate)

---

## Explicitly NOT in Phase C catalog

| Tool | Reason |
|------|--------|
| `search_inventory`, `search_customer`, `search_supplier` | deferred |
| `explain_outbound_failure` | L1 / later phase |
| `prepare_*_draft`, `create_*` | L2/L3 — forbidden |
| Daily Workspace tools | later analysis/frontend |

---

## Architecture guarantees

| Check | Result |
|-------|--------|
| Direct Mapper access from tools | **NONE** |
| Direct SQL / JDBC from tools | **NONE** |
| Business write side effects in tools | **NONE** (query Facades / query Service only) |
| Inventory formula added in tools | **NO** |
| Frontend | **NOT STARTED** |
| Business DB schema modified | **NO** |
