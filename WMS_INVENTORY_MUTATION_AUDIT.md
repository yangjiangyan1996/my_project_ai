# WMS Inventory Mutation Audit

> Branch: `fix/wms-p0-baseline-20260914` @ `ff080f78`  
> Mode: **read-only** against current HEAD (stash NOT restored)  
> Date: 2026-09-14

---

## 1. Real inventory model (from Entity)

### Tables & fields (confirmed)

| Layer | Table | Entity | Qty fields |
|-------|-------|--------|------------|
| Tenant total | `ck_inventory` | `Inventory` | `quantity`, `lockedQuantity` |
| Warehouse | `ck_inventory_warehouse` | `InventoryWarehouse` | `quantity`, `lockedQuantity` |
| Batch | `ck_inventory_batch` | `InventoryBatch` | `quantity`, `lockedQuantity` |
| Shelf | `ck_inventory_shelf` | `InventoryShelf` | `quantity`, `lockedQuantity` |
| Ledger | `ck_inventory_transaction` | `InventoryTransaction` | `changeQuantity`, `balanceQuantity`, `beforBalanceQuantity` |
| Outbound lock | `ck_inventory_lock` (+ log) | `InventoryLock` | `lockQuantity`, `unlockQuantity` |
| Stocktake soft lock | `ck_stock_take_lock` | `StockTakeLock` | scope/status (not qty) |

**No DB fields:** `availableQuantity`, `frozenQuantity`, `occupiedQuantity`, `physicalQuantity`, `version`.

### Hierarchy

```text
ck_inventory (tenant × product)
    └── ck_inventory_warehouse (tenant × warehouse × product)
            ├── ck_inventory_batch (… × batchNo)
            └── ck_inventory_shelf (… × shelfId × batchNo)
```

Ledger `ck_inventory_transaction` is an **append-only auxiliary ledger** (balance snapshots), **not** the authoritative balance store.

### Quantity formulas — **NOT_FORMALLY_DEFINED** (conflict)

Observed behaviors:

| Actor | Meaning of `quantity` when locking |
|-------|-------------------------------------|
| `CkInventoryLockService.updateAllInventoryLockedQuantity` | On lock: **`quantity -= lockQty`** and **`lockedQuantity += lockQty`** |
| Unlock | Reverse: `quantity +=`, `lockedQuantity -=` |
| Display `CkInventoryFacade` | `available = quantity - lockedQuantity` |
| Lock availability check | also `quantity - lockedQuantity` |

**CONFLICT:** If lock already moves stock out of `quantity` into `lockedQuantity`, available should be ≈ `quantity`, **not** `quantity - lockedQuantity` (that double-counts).

**Implied intended (if lock path is “truth”):**

```text
physical ≈ quantity + lockedQuantity
available ≈ quantity          # free pool after lock moved qty
locked    = lockedQuantity
frozen    = NOT_FORMALLY_DEFINED (no field)
```

**Implied by display/check code:**

```text
physical ≈ quantity
available = quantity - lockedQuantity
locked    = lockedQuantity
```

Until one formula is chosen and all writers aligned, **available is unreliable**.

### Source of truth

```text
CURRENT_SOURCE_OF_TRUTH:
ck_inventory / ck_inventory_warehouse / ck_inventory_batch / ck_inventory_shelf
quantity (+ lockedQuantity) rows, mutated by Java select→modify→updateById

EXPECTED_SOURCE_OF_TRUTH:
Same balance tables, with ONE canonical mutation gateway + CAS/optimistic updates;
ck_inventory_transaction as audit ledger only

InventoryHolder:
KEEP (as primary approve gateway) + harden

Direct Mapper / Facade Inventory Mutation (CkInboundFacade legacy, CKProductFacade import, InventoryUpdateHelper):
MIGRATE into InventoryHolder (or shared gateway) then REMOVE duplicates
```

Ledger classification: **B — operation ledger / balance snapshot**, not the sole source of truth.

---

## 2. All inventory write entry points

| ID | Business action | API / entry | Facade | Inventory method | Tables | TX | Ledger | Idempotent |
|----|-----------------|-------------|--------|------------------|--------|----|--------|------------|
| M01 | Inbound create with status=3 | `POST /api/auth/inbound/create` | `CkInboundFacade.create` | **legacy** `updateInventoryAndTransaction` | inv, wh, batch, txn (**no shelf**) | `@Transactional` | YES (partial) | NO |
| M02 | Inbound update → status 3 | `POST /api/auth/inbound/update` | `CkInboundFacade.update` | legacy same | same | yes | YES | NO |
| M03 | Inbound update leave status 3 | same | `rollbackInventory` | direct qty − | inv (+txn soft-delete) | yes | soft-del | NO |
| M04 | Inbound approve | `POST /api/auth/inbound/approveOk` | `CkInboundFacade.approveOk` | **`InventoryHolder.updateAddInventoryForApprove`** | inv+wh+shelf+batch+txn | yes | YES | **NO** (status check disabled) |
| M05 | Outbound create (sale/prod) | create APIs | `CkOutboundFacade` | `lockForSale/ProductionOutbound` | lock + **qty− / locked+** on 4 layers | yes | lock log | weak |
| M06 | Outbound update status=3 | update APIs | `CkOutboundFacade` | `InventoryHolder.updateSub…` | 4 layers + txn | yes | YES | NO |
| M07 | Outbound approve | `POST .../outbound/approveOk` | `approveOk` | unlock then `updateSub…` | unlock restore qty then deduct | yes | YES | **NO** (validate bug) |
| M08 | Outbound cancel/relock | update/delete paths | unlock/relock | lock service | lock + qty | mixed | lock log | weak |
| M09 | Adjust execute | `GET/POST adjust/execute` | `CkAdjustOrderFacade.execute` | `InventoryUpdateHelper` + txn build | 4 layers + txn | yes | YES | status-based |
| M10 | Product Excel init in/out | product import | `CKProductFacade` | `updateInventoryForInbound/Outbound` | 4 layers + txn | caller | YES | NO |
| M11 | Stocktake approve init | stock approve | `CkStockFacade` | `createStockTakeLock` + **empty** `lockInventoryForStock` | `ck_stock_take_lock` only | yes | N/A | soft lock idempotent check |
| M12 | Stocktake complete | complete | release soft lock | no balance mutate in complete itself* | soft lock release | yes | — | — |

\*盘盈盘亏落地通过后续调整单 `M09`（现有设计），非 Holder 直接改。

**HEAD 无 AdjustTransfer / StockMove（stash 隔离）。**

---

## 3. Dual-path analysis (Inbound) — **CONFIRMED**

| Question | Answer |
|----------|--------|
| 1. Facade direct write? | Yes: `CkInboundFacade.updateInventoryAndTransaction` (+ private updateInventory/Warehouse/Batch) |
| 2. Holder write? | Yes: `InventoryHolder.updateAddInventoryForApprove` |
| 3. Tables | Legacy: inv+wh+batch+txn, **missing shelf**. Holder: **all four + txn** |
| 4. Both write ledger? | Yes for add paths (legacy also writes txn inside updateInventory) |
| 5. All layers? | **No** — legacy skips shelf |
| 6. Tenant? | Both use `tenantId` on query/create |
| 7. Transaction? | Both under Facade `@Transactional` |
| 8. Same request both? | **No** — mutually exclusive by status path vs approveOk |
| 9. Split by business? | create/update status=3 → A; approveOk → B |
| 10. Canonical? | **Holder closer**; legacy even logs “看看这个方法能被调用到不，如果调用不到，要删掉” |

Outbound also has dual: approveOk vs update with status=3 both call Holder deduct (without necessarily unlock pairing on status=3 path).

Product import is a **third** mutation family.

---

## 4. Event → quantity matrix (code-derived)

Legend: `+` increase, `-` decrease, `0` no change, `?` uncertain, `C` conflict / path-dependent.

| Business event | physical(qty) | available (as coded) | locked | frozen | inventory log |
|----------------|---------------:|---------------------:|-------:|-------:|---------------|
| Inbound approve (Holder) | + | + (display) | 0 | N/A | YES |
| Inbound create status=3 (legacy) | + | + | 0 | N/A | YES (no shelf) |
| Outbound lock | − qty / + locked | **C** (display subtracts again) | + | N/A | lock log |
| Outbound unlock (cancel) | + qty / − locked | C | − | N/A | lock log |
| Outbound approve unlock+deduct | unlock restore then − qty | − net | − then 0 | N/A | YES |
| Outbound re-approve (bug) | **− again** | − | 0 | N/A | YES again |
| Adjust execute | ± | ± | 0 | N/A | YES |
| Static stocktake lockInventoryForStock | **0 (stub)** | 0 | 0 | N/A | NO |
| Stocktake soft lock row | 0 | 0 | soft meta | N/A | NO |

---

## 5. Transactions / half-success

- Approve paths generally `@Transactional(rollbackFor=Exception.class)` — good baseline.
- Inbound `approveOk`: inventory first, then status — if status update fails, TX rolls back (OK).
- Inbound production task update: **swallows update failure** (“不抛异常，避免影响审核主流程”) → possible task/inventory inconsistency (P1).
- Lock service: partial unlock failure sets `success=false` but loop may continue for other items (P1).
- Async relock on outbound update: **outside main consistency story** — P1 race/order risk.
- Self-invocation: private mutation methods rely on **outer** Facade TX (OK if only called from transactional Facade methods).

---

## 6. Concurrency

Pattern everywhere:

```text
select → Java arithmetic → updateById(id)
```

**No** `version`, **no** `UPDATE ... WHERE quantity >= ?`, **no** `SELECT FOR UPDATE` on inventory rows.

**Negative / oversell risk: YES** under concurrent outbound approve/lock.

---

## 7. Idempotency

| Path | Mechanism | Verdict |
|------|-----------|---------|
| Outbound approve | Should require `WaitAudit`; **throw missing** | **HIGH** — re-approve can re-deduct |
| Inbound approve | `validateOrderStatusForApprove` **empty** (TODO commented) | **HIGH** — re-approve can re-add |
| Adjust execute | order status gate in execute | MEDIUM |
| Stocktake soft lock | getLockExist / getStockLockExist | OK for soft lock row |
| Ledger | no unique (tenant,order,item,type) proven | MEDIUM duplicate txns |

---

## 8. Outbound `validateAndGetOrder` — **CONFIRMED**

```1421:1435:my-project-backend/src/main/java/com/example/Facade/CkOutboundFacade.java
    private OutboundOrder validateAndGetOrder(OutboundApproveOkReq approveOkReq) {
        ...
        if (!CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode().equals(order.getStatus())) {
            String statusName = CkInOutboundEnums.InOutBoundStatus.getDescByCode(order.getStatus());
        }
        return order;
    }
```

| Q | A |
|---|---|
| Allowed status | Intended: `WaitAudit` (1) only |
| Actual | **Any status** after existence + tenant check |
| Failure mode | `statusName` computed **never thrown** |
| Re-approve impact | Unlock may no-op; **Holder deducts again** → oversell / negative |
| Severity | **P0-A** |

---

## 9. Stocktake `lockInventoryForStock` — **CONFIRMED stub**

```206:209:my-project-backend/src/main/java/com/example/Facade/CkStockFacade.java
    private void lockInventoryForStock(...) {
        //锁定库存
        //考虑在出库的时候，InventoryHolder类中判断有没有锁，有锁就报错
    }
```

| Q | A |
|---|---|
| Caller | `approveAndInitialize` when `TakeType.STATIC_TAKE` |
| Soft lock | `createStockTakeLock` writes `ck_stock_take_lock` |
| InventoryHolder check | **NONE** (grep empty) |
| Inbound/Outbound check soft lock | **NONE** |
| During stocktake, in/out/adjust | **Still allowed** at inventory layer |
| Unlock | soft lock `releaseByStockTakeId` on complete; no qty unlock companion for stub |
| Design intent | Comment: block outbound/move; soft lock “拦新建盘点”; static should lock inventory qty — **unimplemented** |
| Severity | **P0-B** (process integrity) / elevates to P0-A if static take runs while concurrent outbound |

---

## 10. Multi-tenant on mutations

- Queries typically include `tenantId` (`getByProduct(productId, tenantId)`, etc.).
- Updates often `updateById` **by primary key only** after tenant-scoped select — OK if ID not leaked cross-tenant; **IDOR risk if attacker guesses other-tenant inventory id** while holding their JWT (needs exploit path via API) → mark **UNKNOWN→P1** pending IDOR pen-test, not proven P0-A in this pass.
- Outbound `getById(orderId)` without tenant in validate — then compares tenant: **OK check present**.

---

## 11. Inventory P0 grades

### P0-A

1. Outbound approve status validation no-op → repeat deduct  
2. Inbound approve status validation empty → repeat add  
3. Concurrent select-update races → negative / oversell  
4. Lock model vs available formula conflict → wrong availability decisions  

### P0-B

5. Static stocktake inventory lock stub + no outbound soft-lock enforcement  
6. Inbound legacy status=3 path skips shelf → layer imbalance  

### P1

7. Product import third mutation path  
8. Async relock  
9. Adjust helper vs Holder duplication  
10. Production task update swallow errors on inbound approve  

---

*End of inventory mutation audit.*
