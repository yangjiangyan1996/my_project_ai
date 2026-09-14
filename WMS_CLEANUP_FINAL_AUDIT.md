# WMS Cleanup Final Audit

> Branch: `refactor/wms-ai-20260914`  
> Base: `feature-common-20251222`  
> Date: 2026-09-14  
> Mode: **read-mostly audit**; only fixed Cleanup-introduced YAML breakage (`spring:` key).  
> No commit / push / deploy / DB change / Phase-1 P0 fixes.

---

## VERDICT: **PASS_WITH_WARNINGS**

| Gate | Result |
|------|--------|
| Backend `mvn clean compile` (JDK17) | PASS |
| Frontend `npm run build` | PASS |
| Confirmed WMS core accidental deletion | **NONE** |
| Cleanup-introduced P0 (after fix) | **NONE** |
| Cleanup-introduced auth hole | **NONE** (historical anonymous APIs remain; see Phase 1) |

Warnings do **not** block commit of the cleanup itself, but the working tree also contains **out-of-scope WMS WIP** that must be consciously included or excluded when committing.

---

## 1. Diff classification summary

| Class | Meaning | Volume / examples |
|-------|---------|-------------------|
| **A 明确正确** | 纯社区删除 + 必要共享拆依赖 | ~288 `D`：Project/Quan/Chat/Achievement/My/Message/Test…；Authorize 去 `@TaskProgress`；Upload 去 `ImagesMapper`；Router 去社区路由；Welcome 文案 |
| **B 合理需确认** | 白名单增补 reset/askPhone；前端守卫强制登录 | `Config.WHITE_URL` 新增项；router `beforeEach` 收紧 |
| **C 疑似误删** | — | **NONE**（编译无断引用；`CommonController`≠通用层；`CommonConstant`≠`CkCommonConstant`；`CommonFacade` 仍在） |
| **D 超出 Cleanup Scope** | 分支携带的既有 WMS WIP | `CkAdjust*` / `Inventory*` / `StockAdjustment` / `StockMove` / `CkAdjustTransferExt*` / `main.js` baseURL / `package-lock` |
| **E 安全风险** | 历史匿名面（非本轮新开） | `/api/auth/common/**` 上传；`/api/unauth/tenant/getTenantList`；JWT Filter `startsWith("…/**")` 与 Security Ant 不一致 |

---

## 2. Common 命名审计（重点）

| 名称 | 结论 |
|------|------|
| 已删 `CommonController` (`/api/unauth/common/`) | **社区枚举字典 API**（难度/行业/标签/用户类型）→ A 正确 |
| 已删 `constants/CommonConstant` | 仅 `TIME_PER_DAY` / 社区 `PROJECT_URL` → A 正确 |
| 保留 `constants/CkCommonConstant` | WMS SKU/BOM 常量 → 仍被 `CKProductFacade` 使用 |
| 保留 `Facade/CommonFacade` | 用户搜索等共享 → **未删** |
| 保留 `entity/RestBean` / exception advice | 通用返回 → **未删** |

**无“通用基础设施被当成社区 Common 误删”问题。**

---

## 3. AuthorizeController

Diff **仅**：

1. 去掉 `import TaskProgress`  
2. 去掉 `@TaskProgress(category={"denglu"})` on `/register`

| 检查项 | 结论 |
|--------|------|
| 登录 | 仍由 `SecurityConfiguration` formLogin `/api/auth/login` 签发 JWT（含 tenantId） |
| 注册 / reset / ask-code | 方法体未改 |
| refresh token | 项目本无独立 refresh 流程（既有） |
| logout | Security logout 路径未改 |
| JWT 校验 | Filter 未改 |
| 新增未鉴权入口 | **无**（本文件未新增 mapping） |

**本次修改安全。** JWT 黑名单关闭仍为既有 P0，未在本轮触碰。

---

## 4. WHITE_URL 审计

### Before (HEAD)

```text
/api/auth/login
/api/auth/register
/api/auth/ask-code
/api/auth/verifyEmail
/api/auth/sendVerifyEmail
/api/unauth/**
/api/auth/project/simple          ← 社区，已删
/api/unauth/project/**            ← 社区，已删
/api/auth/common/**
/imgs/**
/static/imgs/**
```

### After

```text
/api/auth/login
/api/auth/register
/api/auth/ask-code
/api/auth/askPhoneCode            ← 新增（注册验证码）
/api/auth/verifyEmail
/api/auth/sendVerifyEmail
/api/auth/reset-confirm           ← 新增（与 UnauthController 重置能力重叠）
/api/auth/reset-password          ← 新增
/api/unauth/**
/api/auth/common/**
/imgs/**
/static/imgs/**
```

| URL | 用途 | 需匿名? | 当前匿名? | 建议 |
|-----|------|---------|-----------|------|
| `/api/auth/login|register|ask-*` | 认证 | 是 | 是 | 保留 |
| `/api/auth/reset-*` | 重置密码 | 是 | 是 | 保留；与 `/api/unauth/login/reset-*` 去重（Phase 1） |
| `/api/auth/common/**` | 上传 | **否（理想）** | **是** | Phase 1：收紧为需登录；历史问题 |
| `/api/unauth/**` | 重置 + 租户列表 | 部分 | 是 | Phase 1 审视租户列表 |
| `/api/auth/project*` | 社区 | — | 已移除 | ✅ |

**Cleanup 未新增“匿名写库存/改单据”能力。**

### `cangku UnauthController`（历史）

| API | 风险 | 等级 |
|-----|------|------|
| `POST /api/unauth/login/reset-confirm\|reset-password` | 密码重置（需验证码） | 预期匿名 |
| `GET /api/unauth/tenant/getTenantList` | 返回全部租户 id/name/image | **P1** 信息暴露（历史） |

→ Phase 1 Backlog，**非 Cleanup 新引入**。

---

## 5. UploadController

- 仅移除未使用 `ImagesMapper` 注入。  
- 仍 `UserUtil.getCurrentUser()` + COS key `{dir}{userId}/…`。  
- 因 `/api/auth/common/**` 白名单，**理论上仍可匿名触达**；无用户时可能 NPE（历史）。  
- WMS 商品图/头像调用路径未删。  

**Cleanup 未破坏上传实现；匿名上传为历史债。**

---

## 6. Frontend router / App / net / Welcome

| 项 | 结论 |
|----|------|
| 社区路由 | 已清空；仅 welcome + `ck*` / inbound / outbound / stock |
| 默认 `/` → `CkIndex` | 有效 landing |
| 双守卫 | `router/index.js` 强制登录；`net/index.js` 去掉 quan 白名单 |
| App.vue | 去掉社区 emoji provide；保留 `userInfo` provide |
| Welcome | 文案改为 WMS |
| net API 模块 | 仍为通用 get/post；前端无 `/api/auth/quan|project|chat|achievement` 残留 |

---

## 7. WMS Controllers / Facades 完整性

仍存在（抽样）：

- 主数据：Product / Sku / Warehouse / Shelf / Customer / Supplier / Unit / Tenant / User  
- 入出库库存盘点：Inbound / Outbound / Inventory / Stock / Adjust / AdjustTransfer(Ext)  
- 锁库/库存：`InventoryHolder`、`CkInventoryLockService`  
- 共享：Authorize / Upload / User / FsFacade / CommonFacade  

**无核心 Controller/Facade 被 Cleanup 删除。**

依赖图 spot check：无对已删 `ProjectFacade`/`ImagesMapper`/`TaskProgress` 等的编译引用。

---

## 8. Call-graph spot check（静态）

| 链路 | 结果 |
|------|------|
| Login → Security → JwtUtils(tenantId) | 完整 |
| Product pageList（Vue→ProductController→CKProductFacade） | 完整 |
| Inbound create/approve | 完整 |
| Outbound create/approve | 完整 |
| Inventory counts/alerts（CkIndex） | 完整 |
| Stock createStockTake | 完整 |

---

## 9. Entity / Mapper

- 社区 Entity/Mapper 成对删除。  
- WMS `ck_*` Entity + `Ck*Mapper` 仍在。  
- 无“WMS Entity 保留但 Mapper 误删”的编译证据。  
- （类名与 Mapper 前缀 `Ck` 不完全一一对应属历史命名，非本轮破坏。）

---

## 10. 社区残留扫描

| 残留 | 分类 |
|------|------|
| `openspec/specs/project-community|quan-community|chat-message|achievement-task` | **文档** |
| `ChatRequest.Message` / DeepSeek | **AI LLM 消息结构**（非 IM） |
| `CkStockTakeTask*` / `ProductionTask` | **WMS 合法** |
| `CkOperationLog` / LogAspect | **WMS 审计** |
| `CommonEnum.IndustryEnum` | JWT/用户资料残留字段用途，**可后续瘦身** |
| 源码 `副业|圈子|QuanList|ProjectFacade` | **NONE** |

---

## 11. AI Infrastructure

| 组件 | 判定 |
|------|------|
| DeepSeekUtils / Config / WebClient / ChatRequest | **保留** — 适合 AI WMS Copilot |
| JimengFacade / JMConfig | **保留但** `NOT_REQUIRED_FOR_AI_WMS_CORE`（图片生成；当前无业务调用方） |

---

## 12. 多租户

`CkInbound/Outbound/Inventory/Stock/Product` Facade 仍大量使用 `tenantId`（抽样计数均 >40）。  
历史漏校验可能仍存在 → Backlog，**非 Cleanup 引入**。

---

## 13. Cleanup 引入问题与修复

| 问题 | 等级 | 处理 |
|------|------|------|
| 删 `auto-publish` 时误删 `spring:`，mail/datasource/jwt 挂到 `deepseek` 下 | **P0** | **已修复** `application-prod.yml` + `application-dev.yml` |

---

## 14. Tests

```text
BASELINE_EXISTING_FAILURE
```

`MyProjectBackendApplicationTests.contextLoads` → `DataSourceBeanCreationException: Failed to determine a suitable driver class`（测试未加载 profile/datasource）。  
**非 Cleanup 引入。** Test baseline unavailable without fixing test config (out of this Change).

---

## 15. Maven / package.json

- 未删除 Redis/Mail/COS/WebClient/AI SDK（正确）。  
- OpenAI pom 依赖仍无代码引用（历史噪音，未删）。  
- 前端未批量删 dependency（正确）；`@kangc/v-md-editor` 等可能社区残留，**待确认，本轮不删**。

---

## 16. Commit readiness

**Ready to commit: YES**（Cleanup 本身 PASS_WITH_WARNINGS）

建议提交时：

1. **优先**只提交社区删除 + 共享拆依赖 + 审计文档 + yml `spring:` 修复。  
2. **单独**处理 Scope D 的库存调整/转移 WIP（或明确同 commit 说明）。  
3. **不要**把含明文密钥的讨论写进提交说明；dev yml 密钥仍是既有 P0。

Recommended message:

```text
refactor(wms): remove community features and isolate WMS core
```

Next: **Phase 1 — WMS P0 correctness and security baseline**（库存双轨、出库校验、盘点锁、JWT 黑名单、匿名上传/租户列表、测试数据源）。

---

## 17. 本轮未做（按要求）

- 未修库存 P0 / 未重构大类 / 未升级框架  
- 未执行 DROP / 未部署 / **未 commit / 未 push**  
- 未开发 AI 功能  
