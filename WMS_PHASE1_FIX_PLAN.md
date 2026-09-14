# WMS Phase 1 — Fix Plan (MINIMAL SAFE CHANGE)

> From audit on `fix/wms-p0-baseline-20260914` @ `ff080f78`  
> **Do not implement until user starts FIX-01.**  
> No AdjustTransfer / StockMove / AI Copilot / God-Facade rewrite.

---

## Principles

1. Correctness before beauty.  
2. One Change ≈ one risk class.  
3. Prefer throw + status CAS over new frameworks.  
4. Prefer gating existing soft locks over inventing freeze engine in FIX-01.  
5. DB migration only when unavoidable; prefer application-level CAS first.

---

## FIX-01 — Outbound approve status validation

| | |
|--|--|
| **问题** | `validateAndGetOrder` 非 `WaitAudit` 时不抛异常 |
| **影响** | 重复审核 → 重复解锁无效果后 **重复扣库存** |
| **修改范围** | `CkOutboundFacade.validateAndGetOrder` (+ optional `update ... WHERE status=WaitAudit`) |
| **风险** | 低；可能暴露原先依赖错误状态也能审核的脏数据流程 |
| **需要测试** | 非 WaitAudit 审核失败；WaitAudit 一次成功；二次审核失败且库存不变 |
| **上线风险** | 低 |
| **DB 变更** | 否（CAS 可用现有 status 字段） |

**最小修复：** `throw new ValidationException(...)`；状态更新改为条件更新，影响行数≠1 则失败回滚。

---

## FIX-02 — Inbound approve status validation

| | |
|--|--|
| **问题** | `validateOrderStatusForApprove` 整段注释 |
| **影响** | 重复审核 → **重复加库存** |
| **修改范围** | `CkInboundFacade.validateOrderStatusForApprove` + approve 状态 CAS |
| **风险** | 低–中；需确认产品允许的审核前状态（WaitAudit vs 注释里曾写的 2） |
| **需要测试** | 与 FIX-01 对称的加库存幂等 |
| **上线风险** | 低 |
| **DB 变更** | 否 |

**最小修复：** 恢复明确允许状态（建议仅 `WaitAudit`，与出库对齐）；去掉“已通过还能再审核”的注释逻辑除非产品书面确认。

---

## FIX-03 — Available / lock quantity model

| | |
|--|--|
| **问题** | 锁库时 `quantity−` 且 `locked+`，展示却用 `quantity−locked` → CONFLICT |
| **影响** | 可用量错误 → 误拒/误放过锁库与出库 |
| **修改范围** | 先文档常量/注释；再改 **一处** 权威 available 计算（Facade 展示 + LockService 校验）对齐选定公式 |
| **风险** | 中 — 改公式会改变现网“看起来的可用” |
| **需要测试** | 锁后 available；解锁后；审核后 |
| **上线风险** | 中 |
| **DB 变更** | 否（公式层） |

**决策前不写大重构。** 两选一写死并全链路统一：

- **A:** `physical = qty + locked`, `available = qty`（匹配当前 lock 实现）  
- **B:** 锁库不再改 `quantity`，只改 `locked`（匹配当前 display）— 改动更大，属后续 Change。

Phase 1 建议先选 **A + 改 display/check**，不动 lock 写路径。

---

## FIX-04 — Stocktake soft-lock enforcement

| | |
|--|--|
| **问题** | `lockInventoryForStock` 空；出入库不看 `ck_stock_take_lock` |
| **影响** | 静态盘点期间仍可出入库/调整 → 账实漂移 |
| **修改范围** | `InventoryHolder` 或 Inbound/Outbound/Adjust Facade 入口查 soft lock |
| **风险** | 低–中；进行中盘点仓库会拒单 |
| **需要测试** | 有 soft lock 时出/入库失败；完成后成功 |
| **上线风险** | 中（运营需知盘点阻塞） |
| **DB 变更** | 否（表已有） |

**最小修复：** 不实现完整 qty 锁；**强制 soft-lock 门禁**。完整 `lockInventoryForStock` 可列为 Change `stocktake-inventory-lock` 后续。

---

## FIX-05 — Retire incomplete inbound legacy path

| | |
|--|--|
| **问题** | create/update `status==3` 走 Facade 私有加库，缺货架层 |
| **影响** | 四层库存不一致 |
| **修改范围** | `CkInboundFacade.updateInventoryAndTransaction`：禁用或改为调用 Holder |
| **风险** | 中 — 依赖“创建即完成”的客户端会变 |
| **需要测试** | status=3 创建走 Holder 且含 shelf；或强制必须走审核 |
| **上线风险** | 中 |
| **DB 变更** | 否 |

**最小修复：** 删除/短路 legacy，统一 `InventoryHolder.updateAdd…`；禁止 silent dual semantics。

---

## FIX-06 — Concurrency CAS on deduct / lock

| | |
|--|--|
| **问题** | select → Java → updateById 竞态 |
| **影响** | 负库存 / 超卖 |
| **修改范围** | Mapper XML/注解：`UPDATE ... SET quantity=quantity-? WHERE id=? AND quantity-locked>=?`（按 FIX-03 公式调整） |
| **风险** | 中 — SQL 与公式绑定 |
| **需要测试** | 并发双扣仅一成功 |
| **上线风险** | 中 |
| **DB 变更** | 可选 version 列；优先无 schema |

---

## FIX-07 — Security baseline

| Item | Minimal change |
|------|----------------|
| Secrets | 移出 git：env；**轮换**已暴露云密钥；dev 用 gitignored override |
| Upload | 从 `WHITE_URL` 移除 `/api/auth/common/**`；`REQUIRE_AUTH`；后缀/MIME 白名单 |
| Tenant list | 保留最小字段 + IP/账号 rate limit；注册仍可用 |
| JWT logout | 打开 Redis blacklist 读写（确认 Redis 可用） |

| | |
|--|--|
| **影响** | 密钥滥用、Token 盗用窗口、匿名上传面、租户枚举 |
| **修改范围** | `Config`, `UploadController`, `JwtUtils`, yml, 可选 filter rate-limit |
| **风险** | 上传需登录；logout 依赖 Redis |
| **需要测试** | 未登录 upload 401；logout 后同 Token 401 |
| **上线风险** | 中 |
| **DB 变更** | 否 |

---

## FIX-08 — Test baseline

| | |
|--|--|
| **问题** | `contextLoads` 依赖本机 MySQL |
| **方案** | **B: Testcontainers MySQL**（首选）；Mock 仅测纯函数校验 |
| **修改范围** | test deps + `@Testcontainers` profile；1–2 个 approve 幂等集成测 |
| **风险** | 低（仅 test） |
| **DB 变更** | 否（容器内） |

不要强行 H2：MyBatis/MySQL 特性多。

---

## Suggested git Changes (independent PRs / commits later)

```text
Change 1  outbound-inbound-approve-status-cas     → FIX-01 + FIX-02
Change 2  inventory-available-model-align         → FIX-03
Change 3  stocktake-soft-lock-enforce             → FIX-04
Change 4  inventory-legacy-inbound-retire         → FIX-05
Change 5  inventory-cas-concurrency               → FIX-06
Change 6  wms-security-baseline                   → FIX-07
Change 7  wms-test-baseline                       → FIX-08
```

Optional later (not Phase 1 P0 minimal): full static stocktake qty lock, unify Adjust/ProductFacade into Holder, God Facade split.

---

## Explicitly out of scope this phase

- Restore stash AdjustTransfer / StockMove  
- AI Copilot  
- Large Facade 拆分  
- Production SQL / deploy / commit / push（除非用户另行指令）

---

## Ready state

Audit complete. **Stop.** Await user: `开始 FIX-01`（或指定 Change）。
