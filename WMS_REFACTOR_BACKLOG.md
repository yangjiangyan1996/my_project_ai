# WMS Refactor Backlog

> 分支：`refactor/wms-ai-20260914`  
> 本轮仅做社区剥离，以下问题记录但不修复。

---

## P0（下一阶段优先 — 来自 PROJECT_REFACTOR_AUDIT）

| ID | 问题 | 位置 |
|----|------|------|
| P0-1 | 入库库存双轨更新 | `CkInboundFacade.updateInventoryAndTransaction` vs `InventoryHolder` |
| P0-2 | 出库审核状态校验未 throw | `CkOutboundFacade.validateAndGetOrder` |
| P0-3 | 盘点静态锁库存空实现 | `CkStockFacade.lockInventoryForStock` |
| P0-4 | JWT 登出黑名单关闭 | `JwtUtils.deleteToken/isInvalidToken` |
| P0-5 | dev 密钥明文入库 | `application-dev.yml` |
| P0-6 | 测试无数据源导致 contextLoads 失败 | `MyProjectBackendApplicationTests`（环境债） |

---

## 本轮剥离中新发现 / 待跟进

| ID | 问题 | 说明 |
|----|------|------|
| B-1 | `ck_role/menu` 未接入鉴权 | 表保留，权限空转 |
| B-2 | 前端 ck 路由 `requiresAuth: false` 与全局守卫冲突 | 本轮守卫已强制登录；meta 字段待统一清理 |
| B-3 | `Account.industryCode` 社区残留字段 | 不删列，后续可废弃 |
| B-4 | `CommonEnum` 中大量社区行业分类 | 仍被 JWT IndustryEnum 使用，可瘦身 |
| B-5 | `JimengFacade` 暂无业务调用方 | 保留作 AI 图片基础设施 |
| B-6 | `DeepSeekUtils` 暂无业务调用方 | 保留作 LLM Client |
| B-7 | MySQL 表名 `redis` | 命名误导，单独评估 |
| B-8 | `ck_transfer_order` 无 Controller | 半废弃，勿与 AdjustTransfer 混淆 |
| B-9 | 端口：yml `spring.port` vs 前端 `localhost:8080` | 环境配置债 |
| B-10 | openspec 社区 specs 仍在仓库 | 文档债务，可归档标记 deprecated |

---

## 多租户巡检（本轮仅记录）

- WMS 侧普遍传 `tenantId`（已确认模式）。
- 未做全量接口跨租户 IDOR 渗透测试 → 待 Phase 1 安全基线。
- 无 `company_id` 字段；租户模型以 `tenant_id` 为准。

---

## 建议下一阶段顺序

1. Phase 1：P0-1 ~ P0-5 + 测试基线  
2. Phase 2：WMS 模块化单体拆分  
3. Phase 3：AI Read-Only Copilot（见 `AI_WMS_ARCHITECTURE_PROPOSAL.md`）
