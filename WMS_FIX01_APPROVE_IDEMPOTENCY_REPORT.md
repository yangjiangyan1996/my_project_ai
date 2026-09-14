# WMS FIX-01 — Approve Idempotency Report

> Branch: `fix/wms-approve-idempotency-20260914`  
> Base: audit commit `32ffe40` on `fix/wms-p0-baseline-20260914`  
> Scope: inbound/outbound approve validation + status CAS only  
> Commit of this fix: **NO** (await review)

---

## Status machine (from code)

`CkInOutboundEnums.InOutBoundStatus`:

| Code | Name | Meaning |
|------|------|---------|
| 0 | WaitSubmit | 待提交(草稿) |
| 1 | WaitAudit | 审核中 |
| 2 | AuditPass | 审核已通过 |
| 3 | InOutboundComplete | 出入库已完成 |
| 4 | Reject | 已拒绝 |
| 9 | Cancel | 已取消 |

**Approve allowed:** only `WaitAudit(1)`  
**Approve target:** `AuditPass(2)`  
(No `company_id` on order tables; tenant field is `tenant_id`.)

---

## Outbound

### 原问题
`validateAndGetOrder` 在非 `WaitAudit` 时仅计算 `statusName`，**不抛异常**，重复审核可再次进入 unlock + `InventoryHolder.updateSubInventoryForApprove`。

### 根因
状态预检失效 + 状态更新在库存 Mutation **之后** + `updateById` 无 expected-status 条件 → 并发/重复均可二次扣库。

### 修复方式
1. 预检：非 `WaitAudit` → `throw ValidationException`  
2. CAS：`WaitAudit → AuditPass`（含 `tenant_id`、`is_deleted=0`），**在库存 Mutation 之前**  
3. CAS `affected rows != 1` → `订单状态已变化，请刷新后重试`，不进入 InventoryHolder  
4. 移除原 `updateOrderToApproved`（状态已由 CAS 写入）

### CAS（等价 SQL）

```sql
UPDATE ck_outbound_order
SET status = 2,          -- AuditPass
    modified_by = ?,
    modified_at = ?,
    remark = ?           -- optional
WHERE id = ?
  AND tenant_id = ?
  AND status = 1         -- WaitAudit
  AND is_deleted = 0
```

实现：`CkOutboundOrderServiceImpl.casUpdateStatusForApprove`（MyBatis-Plus `UpdateWrapper`）。

### 事务边界
`approveOk` 方法级 `@Transactional(rollbackFor = Exception.class)`。  
顺序：validate → items → **CAS** → unlock → deduct → side effects。  
库存失败 → 整单 rollback（含 CAS 状态）。私有方法经代理入口调用，事务生效。

### 测试
`OutboundApproveIdempotencyTest`（4）：首次成功且 CAS→库存顺序；重复预检失败；取消状态失败；CAS 冲突不扣库。

---

## Inbound

### 原问题
`validateOrderStatusForApprove` 整段注释，任意状态可进 `updateAddInventoryForApprove`，再 `updateById` 改状态。

### 根因
预检空实现 + 状态更新在加库之后 + 无 CAS。

### 修复方式
1. 恢复预检：仅 `WaitAudit`  
2. CAS：`WaitAudit → AuditPass`（`tenant_id` + `is_deleted`），**在加库之前**  
3. CAS 失败不进入 InventoryHolder  
4. 去掉 approve 路径上事后 `updateInboundOrderStatus`（方法仍保留未删，其它潜在用途）

### CAS（等价 SQL）

```sql
UPDATE ck_inbound_order
SET status = 2,
    modified_by = ?,
    modified_at = ?
WHERE id = ?
  AND tenant_id = ?
  AND status = 1
  AND is_deleted = 0
```

实现：`CkInboundOrderServiceImpl.casUpdateStatusForApprove`。

### 事务边界
同出库：`@Transactional` on `approveOk`；CAS 与库存同事务；库存失败则状态 rollback。

### 测试
`InboundApproveIdempotencyTest`（4）：对称用例。

---

## CAS service contract tests

`ApproveStatusCasServiceTest`（4）：mapper `update` 返回 1 → true；返回 0 → false；Wrapper 含 status/tenant 条件。

---

## Concurrent approve

**NOT_TESTED**（真实双线程 DB 竞态）。记入 **P1 Test Backlog**。  
逻辑上依赖 DB 行锁 + `WHERE status=1`：至多一行 CAS 成功。

---

## Invariants

| # | Invariant | Status |
|---|-----------|--------|
| 1 | Outbound approve success ≤ 1 | Enforced by CAS |
| 2 | Inbound approve success ≤ 1 | Enforced by CAS |
| 3 | Duplicate approve → inventory Δ = 0 | Precheck/CAS before Holder |
| 4 | CAS fail → no InventoryHolder | Verified by unit tests |
| 5 | Inventory fail → status rollback | Same TX |

---

## Scope check

Changed files only:

- `CkOutboundFacade` / `CkInboundFacade` (approve path)
- `CkOutboundOrderService(+Impl)` / `CkInboundOrderService(+Impl)` (CAS API)
- New unit tests under `src/test/java/...`

No InventoryHolder / stocktake / security / AI / AdjustTransfer / StockMove changes.

---

## Build / tests (this machine)

| Item | Result |
|------|--------|
| `mvn clean compile` | PASS |
| FIX-01 tests (12) | PASS |
| Full `mvn test` (13) | PASS（本机 MySQL `dev` 可用，`contextLoads` 也过） |
| Concurrent DB test | NOT_TESTED |
| Testcontainers | 未引入（Scope 控制） |

> 说明：无 DataSource 环境时 `contextLoads` 仍可能失败；与 FIX-01 无关。

---

## VERDICT

**PASS_WITH_WARNINGS** — 并发真实 DB 双线程未测；生产任务更新仍可能吞失败（既有 P1，非本 Change）。
