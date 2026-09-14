# Project Refactor Audit

> 分析日期：2026-09-14  
> 分析范围：`my-project-backend` + `my-project-frontend` + `openspec` + 配置/中间件/部署线索  
> 原则：只读静态逆向分析；未修改业务代码；未执行 DB 变更；未 commit / push。  
> 证据优先级：真实代码 > openspec > README / 旧架构报告。冲突处已标注。

---

## 1. Executive Summary

本项目是一个**前后端分离的复合型单体应用**：同一套 Spring Boot + Vue3 同时承载「副业社区 / 圈子 / 聊天 / 成就」与「多租户仓储 WMS」，并接入 DeepSeek / 即梦 / 腾讯云 COS&SMS。

**核心结论（已确认）：**

1. **不应整体重写。** 仓储库存五层模型、出库锁库、Facade 编排方向是正确且有业务价值的；推倒重来的数据迁移与回归成本远高于收益。
2. **应做渐进式现代化：模块化单体 + 统一公共能力 + 拆巨型 Facade + 补测试基线。**
3. **最大风险不在“技术老旧”，而在：库存变更双轨、盘点锁库空实现、出库审核状态校验失效、密钥入库、JWT 黑名单关闭、几乎无测试、前端超大页面复制。**
4. **文档与代码存在多处冲突**：README 仍是模板描述；openspec 已规划 AI Agent Foundation，但代码未落地；旧仓储报告称 JWT 黑名单“活跃”，代码中 Redis 黑名单写入/校验均被注释。

**建议路线：方案 A（渐进重构）为主，辅以方案 B（按业务域包级模块化），明确拒绝方案 D（整体重写）。**

---

## 2. 项目定位

| 项 | 结论 | 置信度 |
|----|------|--------|
| 项目名称 | `my_project_ai` / `my-project-backend` + `my-project-frontend` | 已确认 |
| 用途 | 副业社区平台 + 多租户仓储管理系统 + AI 内容生成 | 已确认 |
| 核心业务 | 仓储入出库/库存/盘点/调整；社区项目发布撮合；圈子帖子 | 已确认 |
| 主要用户角色 | 注册用户、项目成员、租户仓储操作员/审批人、ADMIN（`account.role` 字符串） | 已确认 / 部分推测 |
| 架构形态 | **单体后端 + 单前端**，Maven 单模块，非微服务 | 已确认 |
| 独立 Worker / Scheduler | 无活跃 Worker；`@Scheduled` 任务体已注释 | 已确认 |
| 多前端 | 无 UniApp / 小程序独立工程 | 已确认 |

**与文档冲突：**

| 来源 | 说法 | 代码现实 |
|------|------|----------|
| `README.md` | JWT 模板：注册登录 + 邮件队列 | 已演化为大型 WMS + 社区 + AI |
| `openspec/project.md` | 含 `ai-enterprise-agent-foundation` / `ai-warehouse-agent-workbench` | 仅有 openspec，未见 AgentRegistry 等实现 |
| `当前仓储系统架构分析报告.md` | JWT 黑名单 Redis 活跃 | `JwtUtils.deleteToken/isInvalidToken` 中 Redis 操作已注释（`todo yang redis部署后open`） |

---

## 3. 技术栈

### 3.1 后端（已确认，依据 `pom.xml`）

| 技术 | 版本/状态 |
|------|-----------|
| Java | 17 |
| Spring Boot | 3.1.2 |
| Spring Cloud | **无** |
| 构建 | Maven（单模块） |
| ORM | MyBatis-Plus 3.5.3.1 |
| Security | Spring Security + Auth0 JWT 4.3.0 |
| Redis | `spring-boot-starter-data-redis`（配置缺省，黑名单逻辑关闭） |
| MQ | `spring-boot-starter-amqp`（邮件队列声明存在，生产/消费路径基本旁路） |
| WebSocket | **无** |
| 调度 | `@EnableScheduling` 存在，任务注释；无 Quartz / XXL-JOB |
| ES / Milvus | **无** |
| OSS | 腾讯云 COS `cos_api` + STS |
| AI | DeepSeek（WebClient）；即梦/火山 `volc-sdk-java`；OpenAI SDK 在 pom 但无 Java 调用 |
| 其他 | FastJSON2、Hutool、EasyExcel、SpringDoc、邮件、腾讯云 SMS |

### 3.2 前端（已确认，依据 `package.json`）

| 技术 | 状态 |
|------|------|
| Vue | 3.3.x |
| 构建 | Vite 4 |
| UI | Element Plus |
| Router | Vue Router 4 |
| 状态 | **无 Pinia / Vuex**（`src/store/index.js` 空文件） |
| HTTP | Axios（`src/net/index.js` 回调封装） |
| TS | **无** |
| 编辑器 | WangEditor、v-md-editor |
| 图表 | ECharts |

### 3.3 中间件与外部依赖

```
Vue3 (localhost:8080 axios baseURL)
    ↓ HTTP
Spring Boot（配置写 spring.port:9090，键名疑似无效；实际端口 待验证）
    ↓
MySQL `fuye`
Redis（默认 localhost，黑名单关闭）
RabbitMQ（mail 队列，实际发信多为同步直调）
腾讯云 COS / SMS
DeepSeek API
火山引擎即梦
```

---

## 4. 目录与模块职责

```
my_project_ai/
├── my-project-backend/          # 唯一后端服务
├── my-project-frontend/         # 唯一 Web 前端
├── openspec/                    # 领域 Spec / Change（规范层，非运行代码）
├── 当前仓储系统架构分析报告.md   # 2026-06 仓储专项分析
└── PROJECT_REFACTOR_AUDIT.md    # 本报告
```

### 4.1 后端包职责表

| 模块/包 | 职责 | 核心代码 | 是否被依赖 | 问题 |
|---------|------|----------|------------|------|
| `controller` / `controller/cangku` | HTTP 入口 | ~31 Controller | 前端调用 | 部分 Controller 过重；类名拼写错误；try/catch 吞异常模式普遍 |
| `Facade` | 跨表编排、状态机 | 30 个 Facade，最大 2644 行 | Controller 强依赖 | God Facade；库存逻辑与 Holder 重叠 |
| `service` / `impl` | 单表 CRUD / 领域服务 | ~76 接口 + ~77 实现 | Facade | 多数偏薄；复杂逻辑上浮到 Facade |
| `mapper` | MyBatis-Plus DAO | ~80 Mapper + 少量 XML | Service | XML 极少，复杂查询散落 Facade |
| `entity` | 表实体 + Req/VO 混放 | ~352 类，`@TableName` ~91 表 | 全层 | **dto 包名下实际是表实体**；Entity 命名混乱 |
| `holder` | 库存更新/锁库编排 | `InventoryHolder`、`CkInventoryLockService` 等 | Facade | 与 Facade 边界模糊；锁服务 1484 行 |
| `filter` | Security / JWT / CORS / 限流 / 请求日志 | `SecurityConfiguration` 等 | 全局 | JWT 黑名单失效；白名单过宽 |
| `config` | Bean / 第三方配置 | DeepSeek、COS、Rabbit 等 | 全局 | 密钥来自 yml |
| `job` | 定时任务 | `AutoPublishQuanJob` | 配置开关 | **全部注释，未运行** |
| `listener` | MQ 消费 | `MailQueueListener` | 邮件 | `@RabbitListener` 注释，同步调用 |
| `utils` / `enums` | 工具与枚举 | JwtUtils、Snowflake 等 | 全局 | 工具类偏多 |
| `aop/task` | 日志切面 | `LogAspect` | 可选 | 与 RequestLogFilter 职责重叠 |

**模块职责混乱点（已确认）：**

- 复杂业务集中在 Facade（符合 openspec 约定），但部分 Facade 已膨胀为 God Class。
- `entity.cangku.dto` 实际是 DB Entity，非传输 DTO。
- `CkCommentFacade` 名称为评论，实际承担货架分配相关逻辑（误导）。
- `CkStockTakeTaskFacade` 为空壳。
- 无真正的 common/framework 子模块隔离；单体包内扁平堆叠。
- **无 Maven 多模块循环依赖问题**（因为根本不是多模块）——包级耦合通过 Facade 互相注入体现。

### 4.2 前端目录职责

| 目录 | 职责 | 问题 |
|------|------|------|
| `src/views/ck/**` | 仓储 UI（约 40 页） | 超大 SFC、大量复制 |
| `src/views/*` | 社区/圈子/个人中心 | 超大页（My / UserProfile / Index） |
| `src/views/welcome` | 登录注册 | 正常 |
| `src/net` | Axios 封装 | 无按域 API 模块，URL 散落组件 |
| `src/components` | 仅约 9 个可复用组件 | 复用率极低 |
| `src/store` | 空 | 无全局状态管理 |
| `src/router` | 路由 + 守卫 | ck 大量 `requiresAuth: false`；双守卫 |
| `src/hooks` | `useUserInfo` | 与 App.vue provide 双轨用户信息 |

---

## 5. 真实业务能力地图

```text
A. 用户与租户（核心-辅助）
├── 注册 / 登录 / 登出 / 重置密码
├── 当前用户信息
├── 租户信息维护（ck_tenant）
└── account.role 粗粒度角色

B. 副业项目社区（核心-社区）
├── 项目发布 / 详情 / 列表
├── 点赞 / 收藏 / 评论 / 关注
├── 申请加入 / 审批
├── 成员分组
├── 技能匹配 / 排行榜
└── AI 辅助生成项目内容（DeepSeek / 即梦）

C. 圈子社区 quan（核心-社区）
├── 圈子创建 / 关注 / 管理
├── 帖子（tie）发布 / 置顶 / 热帖
├── 评论 / 点赞 / 收藏
└── （定时自动发帖：代码注释，疑似废弃）

D. 消息与聊天（辅助）
├── 站内消息
├── HTTP 会话聊天（非 WebSocket）
└── 消息已读状态

E. 成就任务（辅助）
├── 任务定义 / 进度
├── 积分 / 徽章
└── 前端 GrowthCenter 未挂路由（疑似废弃）

F. 仓储主数据（核心-WMS）
├── 仓库 / 货架 / 货架区域
├── 商品 / 分类 / BOM / 图片
├── SKU / 客户 SKU 映射
├── 供应商 / 客户 / 单位
└── 推荐规则

G. 入库（核心-WMS）
├── 采购 / 生产 / 退货 / 调拨入库（orderType 区分）
├── 创建 / 更新 / 审核加库存
└── 货架分配

H. 出库（核心-WMS）
├── 销售出库 / 生产领料
├── 锁库 → 审核 → 解锁扣库存
├── Excel 导入导出
└── 生产任务关联

I. 库存（核心-WMS）
├── 总/仓/架/批次五层库存
├── 流水 / 预警 / 批次分配校验
└── 锁库记录

J. 盘点（核心-WMS，成熟度中等）
├── 创建盘点 / 审批初始化 / 任务分配
├── 实盘录入 / 完成
└── 静态锁库方法体为空（风险）

K. 库存调整 / 库内转移（核心-WMS）
├── 手工调整单 create/approve/execute
└── AdjustTransferExt 库内转移扩展

L. 调拨单表 ck_transfer_order（疑似历史/半成品）
├── 有 Entity/Service/Mapper
└── 无独立 Controller；业务更多走入出库 orderType 或 AdjustTransferExt

M. 审批流表 ck_approval_*（辅助/半接入）
├── 表与前端 ApprovalConfigDialog 存在
└── 与单据状态机并存，完整度 待验证

N. 仓储 RBAC 表 ck_role/menu（疑似空转）
├── 有实体
└── 无 Controller/Facade 接入鉴权

O. 文件存储（辅助）
└── COS 上传 / STS 临时凭证

P. AI Agent Foundation / Warehouse Workbench（规划中）
└── 仅 openspec，代码未落地 → 历史规划 / 未实现
```

**重复业务信号：**

- 入库：`CkInboundCreate.vue` 与 `inbound/CkInboundPurchase|Production|Return|Transfer.vue` 多套并行。
- 出库：`CkOutboundCreate.vue` 与 `SalesOutboundCreate` / `ProductionPickingCreate` 并行。
- 库存变更：`CkInboundFacade.updateInventoryAndTransaction` vs `InventoryHolder.updateAddInventoryForApprove`。

---

## 6. 核心业务链路（真实路径）

### 链路 1：销售出库创建 → 审核扣库存（核心）

```text
用户：销售出库开单/提交审核
↓
SalesOutboundCreate.vue（6634 行）
↓
POST /api/auth/outbound/createProductionSaleOutBound
POST /api/auth/outbound/approveOk
↓
OutboundContorller.createProductionSaleOutBound / approveOk
↓
CkOutboundFacade.createProductionSaleOutBound / approveOk
↓
validateAndGetOrder → processInventoryLocking → processInventoryDeduction
↓
CkInventoryLockService（holder）+ InventoryHolder 扣减
↓
ck_outbound_order / ck_outbound_order_item / ck_inventory* / ck_inventory_lock*
↓
前端 CkOutboundManage / 出库详情刷新
```

**风险（已确认）：** `validateAndGetOrder` 在状态非 `WaitAudit` 时只取 `statusName`，**未 throw**，非法状态仍可能继续审核。

### 链路 2：采购入库创建 → 审核加库存（核心）

```text
CkInboundPurchase.vue
↓
POST /api/auth/inbound/create
POST /api/auth/inbound/approveOk
↓
InboundController → CkInboundFacade
↓
审核路径调用 inventoryHolder.updateAddInventoryForApprove
创建时若直接完成则可能走 updateInventoryAndTransaction（双轨）
↓
ck_inbound_order* + 五层库存 + ck_inventory_transaction
```

### 链路 3：库存查询 / 预警（核心）

```text
StockManage.vue / CkIndex.vue
↓
POST /api/auth/inventory/pageList
POST /api/auth/inventory/alerts
↓
InventoryController → CkInventoryFacade
↓
多表聚合查询（存在全量商品加载风险）
↓
ck_inventory* / ck_product
```

### 链路 4：盘点全流程（核心，有空洞）

```text
Stock*Wise.vue / StockManage.vue / StockOperateItem.vue
↓
POST /api/auth/stock/createStockTake
GET  /api/auth/stock/apprroveAndInitialize
POST /api/auth/stock/assignTask
POST /api/auth/stock/executeStockTakeItem
GET  /api/auth/stock/completedStock
↓
CkStockController → CkStockFacade
↓
lockInventoryForStock() 方法体为空 ← P0
↓
ck_stock_take*
```

### 链路 5：手工库存调整（核心）

```text
StockAdjustment.vue
↓
POST /api/auth/adjust/createManual
GET  /api/auth/adjust/submitApprove
POST /api/auth/adjust/approveOk
GET  /api/auth/adjust/execute
↓
CkAdjustOrderController → CkAdjustOrderFacade
↓
InventoryUpdateHelper.updateInventoryByAdjustItem
↓
ck_adjust_order* + 库存表
```

### 链路 6：库内转移调整（核心新增）

```text
StockMove.vue
↓
POST /api/auth/adjust/transfer/...
↓
CkAdjustTransferExtController → CkAdjustTransferExtFacade
↓
ck_adjust_transfer_ext + adjust 单据
```

### 链路 7：用户登录（基础设施）

```text
LoginPage.vue → login() in net/index.js
↓
POST /api/auth/login（Spring Security formLogin）
↓
SecurityConfiguration.handleProcess → JwtUtils.createJwt
↓
Token 存 localStorage/sessionStorage key=authorize
↓
JwtAuthenticationFilter 校验后续请求
```

**注意：** `invalidateJwt` 黑名单写入被注释 → 登出后 token 在过期前仍可能有效（已确认）。

### 链路 8：项目发布 / 互动（社区核心）

```text
IndexView / ProjectDetail / Create*
↓
/api/auth/project/* 或 /api/unauth/project/*
↓
ProjectController → ProjectFacade
↓
projects / projects_detail / project_* 互动表
↓
可选 DeepSeekContentService / JimengFacade
```

### 链路 9：圈子发帖（社区核心）

```text
QuanList / QuanDetail / QuanTieDetail
↓
/api/auth/quan/* 、/api/unauth/quan/*
↓
QuanController / UnauthQuanController → QuanFacade / TieFacade
↓
quan_bars / quan_bar_tie / quan_tie_*
```

### 链路 10：文件上传 COS（辅助）

```text
各表单上传组件 → UploadController / FsFacade
↓
腾讯云 COS（STS 临时密钥）
↓
风险：FsFacade System.out 打印 tmpSecret*
```

---

## 7. 数据库模型

### 7.1 主要表（按域）

| 表名 | 业务含义 | 核心字段（据 Entity） | 关联 | 风险 |
|------|----------|----------------------|------|------|
| `account` | 用户账号 | id, tenant_id, password, role | ck_tenant, ck_user_role | 角色字符串与 RBAC 表双轨 |
| `ck_tenant` | 租户 | id, name | 几乎所有 ck_* | 租户隔离依赖应用层 |
| `ck_warehouse` / `ck_shelves` / `ck_shelf_zone` | 仓/架/区位 | tenant_id | inventory_shelf | 货架模型曾迁移（ShelfZone） |
| `ck_product` / BOM* | 商品与物料清单 | minStock 等 | inventory | 大宽表风险 待验证 |
| `ck_inbound_order(+item)` | 入库单 | orderType, status | inventory | 无独立采购单表 |
| `ck_outbound_order(+item,+sale_ext)` | 出库单 | orderType, status | lock/inventory | 状态机靠枚举魔法数 |
| `ck_inventory` 等五层 | 库存真相源 | qty / available | transaction | 一致性靠 Facade；双轨更新 |
| `ck_inventory_lock(+log)` | 锁库 | source_id | outbound | 无超时自动释放任务 |
| `ck_stock_take*` | 盘点 | approval/take status | adjust | 静态锁空实现 |
| `ck_adjust_order*` / `ck_adjust_transfer_ext` | 调整/转移 | | inventory | 与 transfer_order 概念重叠 |
| `ck_transfer_order*` | 调拨单 | | | **无 Controller，疑似半废弃** |
| `ck_approval_*` | 审批流 | | | 与单据 status 并存 |
| `ck_role/menu/role_menu/user_role` | WMS RBAC | | | **未接入鉴权** |
| `projects*` / `quan_*` / `chat_*` / `task_*` | 社区/聊天/成就 | | | 与 WMS 同库同实例 |
| `redis` | MySQL 伪 KV | k,v,e | AccountService | 命名严重误导 |
| `ai_recommend_logs` | AI 推荐日志 | | | 使用面 待验证 |

### 7.2 模型问题清单

| # | 问题 | 结论 |
|---|------|------|
| 1 | 冗余 | 调拨：`ck_transfer_*` vs 入出库 orderType vs `adjust_transfer_ext` 概念重叠 |
| 2 | JSON 字段 | 部分扩展字段存在，未系统性扫描全部 JSON 列 → **待验证** |
| 3 | 索引 | 无 migration/schema 入库；索引是否充足 **待验证（需 DBA/EXPLAIN）** |
| 4 | 字符串关联 | `account.role` 字符串；部分编码用枚举 code | 已确认有魔法数枚举 |
| 5 | 唯一约束 | 单据号等依赖代码生成雪花/序列表 `ck_serial_number` | 库级约束 **待验证** |
| 6 | 软删除 | **无 `@TableLogic`**；删除多为物理删或状态字段 | 已确认不统一 |
| 7 | created_by/updated_by | 仓储单据普遍有；社区表不完全一致 | 部分确认 |
| 8 | tenant_id | 仓储侧广泛使用；**无 company_id** | 已确认 |
| 9 | 主键 | 雪花/自增混用风险需按表核对 | 待验证 |
| 10 | 状态字段 | `CkInOutboundEnums` / `CkStockTakeEnums` 多套 code | 已确认 |
| 11 | 魔法数字 | 状态/orderType 以 Integer code 存库 | 已确认 |

**Domain 与表一致性：** Java `@TableName` 与业务表大体一致；但 `entity...dto` 命名与真实职责不一致，易导致 AI/新人误改。

---

## 8. 前端架构

### 8.1 规模

- Views：约 **65** 个业务页 + components 约 **9**
- 路由：约 **73**，全部懒加载
- 仓储路由占多数；社区为第二域

### 8.2 API 层

- 统一封装：`get/post` 回调风格，期望 `{ code: 200, data, message }`
- URL **散落在 SFC**，无 `api/inbound.js` 等领域层
- 部分页面直接 `axios` 下载/导入
- baseURL 硬编码 `http://localhost:8080/`（与后端配置意图冲突）

### 8.3 状态与权限

- Token：`authorize` 于 local/sessionStorage
- 用户信息：`useUserInfo` + `App.vue provide` **双轨**
- 无 Pinia；权限多为页面内 `role === 'ADMIN'` 或硬编码 `hasApprovePermission = true`
- ck 路由约 **45** 处 `requiresAuth: false`（依赖后端 JWT）

### 8.4 前端重构 TOP 10

| # | 文件 | 行数 | 问题 |
|---|------|-----:|------|
| 1 | `SalesOutboundCreate.vue` | 6634 | 上帝页面，出库核心，难测难改 |
| 2 | `StockAdjustment.vue` | 4486 | 调整单巨型表单 |
| 3 | `UserProfile.vue` | 3326 | 社区巨型页 |
| 4 | `My.vue` | 3280 | 与 Profile 职责重叠 |
| 5 | `CkOutboundCreate.vue` | 3185 | 与 Sales/Picking 重复 |
| 6 | `ProductionPickingCreate.vue` | 2955 | 出库族复制 |
| 7 | `StockManage.vue` | 2861 | 盘点管理复杂度高 |
| 8 | `StockMove.vue` | 2401 | 新转移页，与调整逻辑相似 |
| 9 | 入库族 Purchase/Production/Return/Transfer | ~2k each | Return≈Transfer 近复制 |
| 10 | `ProductForm.vue` | 2206 | 组件本身过大 |

---

## 9. API 设计

### 9.1 统计（已确认）

| 方法 | 数量 |
|------|-----:|
| POST | 133 |
| GET | 92 |
| PUT | **0** |
| DELETE | **0** |
| 合计 | ~225 |

按域：仓储 `controller/cangku` 约占一半以上；社区 Project/Quan/My/Chat 次之。

### 9.2 风格问题

1. REST 不统一：更新/删除几乎全用 POST。
2. 查询大量用 POST `/pageList`（可接受但需规范）。
3. 命名混乱：`createProductionSaleOutBound`、`apprroveAndInitialize`、`allocateIShelfnventoryQuantity`。
4. 返回统一 `RestBean`（较好）。
5. 分页模式大体统一但 Req 命名不齐。
6. 校验：`@Valid` + `ValidationException`；业务异常滥用 ValidationException。
7. 无统一业务错误码枚举（多靠 message 字符串）。
8. DTO/VO/Entity 混用；包名误导。
9. 部分接口可能直接暴露实体字段（待按接口细查）。
10. Controller 相对 Facade 较薄，但 try/catch 模板重复严重。

### 9.3 最混乱 API TOP 20（代表）

| # | 接口 | 问题 |
|---|------|------|
| 1 | `POST .../outbound/createProductionSaleOutBound` | 命名像生产却是销售 |
| 2 | `POST .../outbound/createProductionPickingOutBound` | 拼写 OutBound |
| 3 | `GET .../stock/apprroveAndInitialize` | 拼写错误；GET 做写操作 |
| 4 | `GET .../adjust/execute` | GET 执行变更 |
| 5 | `GET .../adjust/submitApprove` | GET 提交审批 |
| 6 | `GET .../stock/submitApproval` | 同上 |
| 7 | `GET .../stock/completedStock` | GET 完成盘点 |
| 8 | `POST .../inbound/allocateIShelfnventoryQuantity` | 拼写严重错误 |
| 9 | `GET .../outbound/detail` vs `detailNew` | 双详情并存 |
| 10 | `POST .../outbound/delete` | 非 DELETE 方法 |
| 11 | `/api/auth/common/**` 白名单 | auth 路径却 permitAll |
| 12 | `TestController` `/api/unauth/project/publishProject` 等 | 未鉴权测试入口 |
| 13 | `TestController` `/jimengPicture` `/sms` | 可触发外部计费能力 |
| 14 | 入库 create 多 orderType 共用 | 契约靠前端约定 |
| 15 | `Unauth*` 大范围公开读 | 社区暴露面大，需产品确认 |
| 16 | 调整 submit/execute 用 GET | CSRF/缓存风险（虽 JWT） |
| 17 | ProductController 23 端点 | 商品域 API 膨胀 |
| 18 | ProjectController 28 端点 | 社区 API 膨胀 |
| 19 | Inventory 预警/统计/分页混杂 | 查询 API 边界不清 |
| 20 | `/api/auth` vs `/api/unauth` 前缀语义 | 与白名单叠加难推理 |

---

## 10. 代码质量

### 10.1 超大类 TOP（后端）

| 文件 | 行数 | 职责 | 问题 |
|------|-----:|------|------|
| `CkOutboundFacade` | 2644 | 出库全生命周期 | God Class；事务/锁/Excel 耦合 |
| `CKProductFacade` | 2111 | 商品+BOM+导入 | 职责过多 |
| `CkInventoryFacade` | 1597 | 查询+预警+分配 | 查询性能风险 |
| `CkInventoryLockService`(holder) | 1484 | 锁库 | 与 Facade 边界糊 |
| `CkInboundFacade` | 1155 | 入库+库存更新 | 双轨库存更新 |
| `ProjectFacade` | 1070 | 项目社区 | 社区 God Class |
| `CkStockFacade` | 945 | 盘点 | 含空实现 |
| `CkAdjustOrderFacade` | 662 | 调整 | 与 TransferExt 复制 |

单方法超长：出库审核、锁库、Excel 导入等普遍 >100 行量级（抽样已确认）。

### 10.2 坏味道摘要

- God Facade / 巨型 Vue SFC
- `ValidationException` 充当业务异常
- Controller 大面积 `catch (Exception)` 转 `RestBean.failure`
- Bean 拷贝 / 手工 set 混杂
- 魔法状态码
- 命名拼写债（Contorller）
- MQ/定时/Redis 黑名单“半残”
- 测试几乎为零（2 个 test 文件，无有效覆盖）

---

## 11. 异常体系

**当前实际链路：**

```text
业务 throw ValidationException("...")
  或 Controller try/catch Exception
↓
ValidationController(@RestControllerAdvice) 仅处理 ValidationException
  → log.warn
  → RestBean.failure(400, "请求参数有误")  ← 前端丢失具体原因
↓
其他异常：可能落到 Spring 默认 / Security handler
  → RestBean 401/403
↓
前端 net/index.js：非 200 ElMessage；401 清 token 跳登录
```

| 检查项 | 结论 |
|--------|------|
| 统一异常体系 | **弱**：无 BusinessException / 全局 Handler |
| 吞异常 | Controller 层普遍 catch 后只返回 message |
| printStackTrace | 需持续治理（catch 计数高） |
| catch Exception | 高频 |
| 错误信息回前端 | 业务细节常被替换成固定「请求参数有误」 |
| 错误码 | 非统一枚举 |

---

## 12. 日志体系

| 能力 | 状态 |
|------|------|
| `reqId` | `RequestLogFilter` 写入 MDC（白名单请求跳过） |
| 请求详情日志 | `logRequestStart` 主体**被注释** |
| 响应日志 | 记录完整响应 body（可能含敏感业务数据） |
| LogAspect | 存在，与 Filter 重叠 |
| SQL DEBUG | `application.yml` 全局 Hibernate/mapper DEBUG（生产风险） |
| 密钥打印 | `FsFacade` `System.out` 打印临时密钥 |

**链路追踪能力：** 有 reqId，但缺少统一的 tenantId/userId/taskId 贯穿 MQ/第三方；MQ 实际未走异步，问题被掩盖。

**敏感信息：** 未见系统化脱敏；响应体全量 info 有泄露风险。

---

## 13. 权限与安全

### 13.1 模型

```text
登录 → JWT(claims: id, role, tenantId, ...)
→ JwtAuthenticationFilter
→ 任意已认证即可调大部分 /api/auth/**
→ 仓储接口内部用 UserUtil/tenantId 做租户校验（多数 Facade）
→ 无方法级 @PreAuthorize；ck_role/menu 未接入
```

### 13.2 风险分级

| ID | 风险 | 等级 | 依据 |
|----|------|------|------|
| S1 | `application-dev.yml` 明文密钥入库（DB/JWT/云/AI） | **P0** | 配置文件 |
| S2 | JWT 登出黑名单关闭，token 注销前仍可用 | **P0** | `JwtUtils` 注释 |
| S3 | `TestController` 挂在 `/api/unauth/project/`：发帖/即梦/短信 | **P0** | 未鉴权 + 外部副作用 |
| S4 | 出库审核状态校验未拦截 | **P0** | `validateAndGetOrder` |
| S5 | 盘点 `lockInventoryForStock` 空实现 → 并发盘点/出库风险 | **P0** | `CkStockFacade` |
| S6 | 库存双轨更新 → 数据不一致 | **P0** | InboundFacade vs Holder |
| S7 | `/api/auth/common/**` 白名单过宽 | **P1** | Config.WHITE_URL |
| S8 | 仓储前端路由不强制登录（后端兜底） | **P1** | router |
| S9 | WMS RBAC 表空转，仅靠登录+租户 | **P1** | 无角色 API 鉴权 |
| S10 | CORS `origin: *` | **P2** | application.yml |
| S11 | 生产 JWT/密码空默认值 | **P0/P1** | application-prod.yml |
| S12 | IDOR | **待验证** | 需按接口测跨租户/跨用户 |
| S13 | 文件上传类型/路径校验 | **待验证** | UploadController |
| S14 | SQL 注入 | **低-待验证** | MyBatis-Plus 为主；自定义 XML 少 |
| S15 | XSS | **P2** | 社区富文本编辑器存在 |

---

## 14. 事务

- `@Transactional`：约 **46** 处 / **16** 文件，集中在 Facade 与库存 Holder。
- 超大事务：出库审核、调整创建、BOM 导入、锁库方法体量大。
- 第三方调用：Jimeng/DeepSeek/COS 多数在事务外或独立 Facade（相对较好），但仍需按方法复核。
- 异步：`CkOutboundFacade` 存在异步重新锁库路径 → 与主事务一致性 **P1 风险**。
- **半写入风险场景：** 审核流程中途失败、双轨库存更新、盘点无锁并发出库。

---

## 15. 缓存与 Redis

| 用途 | 实现 | TTL | 状态 |
|------|------|-----|------|
| JWT 黑名单 | StringRedisTemplate | 本应随 token 过期 | **关闭** |
| 登录频率限制 | FlowUtils + Redis | 有 | 依赖 Redis 可用 |
| 接口限流 | FlowLimitingFilter | 有 | 同上 |
| 验证码 | 设计走 Redis/或 MySQL `redis` 表 | — | AccountService 多处注释 |
| 库存缓存 | 无 | — | 直查 DB |
| Spring `@Cacheable` | 无 | — | — |

**风险：** 无 Redis 连接显式配置；黑名单关闭；MySQL 表名 `redis` 误导；无大 Key 治理（因几乎未作业务缓存）。

---

## 16. MQ / 异步

```text
设计：
AccountService → AmqpTemplate → queue "mail" → MailQueueListener

现实：
AmqpTemplate 发送注释
@RabbitListener 注释
改为直接调用 MailQueueListener.sendMailMessage（同步）
```

| 项 | 结论 |
|----|------|
| ACK/重试/死信 | 未真正启用 |
| 幂等 | 不适用（未异步） |
| 消息丢失 | 当前同步路径，MQ 可靠性问题被旁路 |

仓储域无业务 MQ。

---

## 17. 第三方接口

| 第三方 | 用途 | Client | 调用位置 | 超时/重试 | 风险 |
|--------|------|--------|----------|-----------|------|
| DeepSeek | 文案生成 | WebClient | DeepSeekUtils / ContentService / AutoProject | 配置化 | 密钥明文(dev)；无熔断 |
| 即梦/火山 | 图片生成 | volc SDK | JimengFacade / TestController | SDK 默认 | 未鉴权测试入口；费用 |
| 腾讯云 COS | 文件 | COSClient | FsFacade / Upload | — | 临时密钥打印日志 |
| 腾讯云 SMS | 短信 | SMS SDK | SmsUtils / Account / Test | — | 测试入口；费用 |
| QQ 邮箱 SMTP | 邮件 | Spring Mail | MailQueueListener | — | 密码明文(dev) |
| OpenAI SDK | — | pom only | **未使用** | — | 依赖噪音 |

**模式：** 业务 → 专用 Utils/Facade → 第三方；未见统一 HttpClient 治理（超时、重试、熔断、限流）。

---

## 18. 配置与环境

| 项 | 结论 |
|----|------|
| Profile | Maven `dev`(默认) / `prod` + 资源过滤 |
| 密钥 | **dev 明文提交**；prod 用 env，但存在弱默认 |
| `.env` | 支持 optional import；仓库内未发现 `.env` 文件 |
| Nacos | 无 |
| Docker / Nginx / CI | **仓库内均无** |
| 端口 | yml `spring.port:9090`（非标准 `server.port`）；前端 axios `8080` → **环境不一致，待验证实际启动端口** |
| 日志路径 | prod 指向 `/opt/apps/...` 暗示手工部署 |

**报告中不展示任何密钥原文。**

---

## 19. 测试体系

| 类型 | 数量 | 评估 |
|------|------|------|
| 单元测试 | ≈0 有效 | 不能安全重构库存 |
| 集成测试 | 无 | — |
| Controller Test | 无 | — |
| E2E | 无 | — |
| 前端测试 | 无 | package.json 无 test 脚本 |

**最需补测的 TOP 10 核心链路：**

1. 销售出库创建→锁库→审核扣库存  
2. 生产领料出库全流程  
3. 采购入库审核加库存（含货架）  
4. 生产入库 + BOM  
5. 批次分配校验 `checkBatchAllocation`  
6. 盘点审批初始化（含锁库预期行为）  
7. 盘点完成→调整单  
8. 手工调整 execute  
9. 库内转移 AdjustTransferExt  
10. 租户隔离：跨 tenantId 读写拒绝  

**在无上述测试前，禁止大规模改库存 Holder/OutboundFacade。**

---

## 20. 构建与部署

```text
开发
↓ git
↓ 本地：mvn spring-boot:run / IDE；vite dev
↓（无 CI）
↓ 手工：mvn -Pprod package
↓ 手工拷贝 jar 到 /opt/apps/...（推测）
↓（无仓库内 Nginx/Docker）
↓ MySQL / Redis? / RabbitMQ
```

**发布风险：** 不可重复构建环境、无流水线门禁、密钥与配置漂移、前后端端口/baseURL 易错、无健康检查与回滚脚本。

---

## 21. 技术债清单

| ID | 问题 | 等级 | 代码位置 | 影响 | 推荐方案 |
|----|------|------|----------|------|----------|
| TD-01 | 密钥明文入库 | P0 | `application-dev.yml` | 泄露即全线沦陷 | 轮换密钥；gitignore；仅 env/密钥管理 |
| TD-02 | JWT 黑名单关闭 | P0 | `JwtUtils` | 登出无效 | 恢复 Redis 黑名单或短过期+刷新令牌 |
| TD-03 | 未鉴权 TestController | P0 | `TestController` | 滥用发帖/SMS/即梦 | 删除或加 profile 保护 |
| TD-04 | 出库审核状态未拦截 | P0 | `CkOutboundFacade.validateAndGetOrder` | 重复审核/错态扣库存 | 补 throw；加集成测试 |
| TD-05 | 盘点锁库存空实现 | P0 | `CkStockFacade.lockInventoryForStock` | 盘点期间库存被出库 | 实现或明确禁止并发并文档化 |
| TD-06 | 入库库存双轨更新 | P0 | `CkInboundFacade` / `InventoryHolder` | 账实不一致 | 单一入口；旧路径标废弃 |
| TD-07 | 巨型出库 Facade | P1 | `CkOutboundFacade` | 无法维护 | 按命令拆分应用服务 |
| TD-08 | 库存分页全表加载 | P1 | `CkInventoryFacade.pageList` | 性能雪崩 | SQL 层过滤分页 |
| TD-09 | 前端上帝页 | P1 | SalesOutboundCreate 等 | 变更成本极高 | 拆组件 + 组合式 API |
| TD-10 | 无自动化测试 | P1 | `src/test` | 重构不可控 | 先补 10 条核心链路 |
| TD-11 | RBAC 空转 | P1 | `ck_role*` | 安全错觉 | 接入或删除表/文档标明 |
| TD-12 | GET 执行写操作 | P1 | adjust/stock 若干 | 误触发/缓存 | 改为 POST |
| TD-13 | MQ/定时半残 | P2 | listener/job | 认知负担 | 清理或真正启用 |
| TD-14 | MySQL 表名 `redis` | P2 | RedisDTO | 误导 | 重命名需迁移窗口 |
| TD-15 | 异常体系弱 | P2 | ValidationController | 排障困难 | BusinessException + 错误码 |
| TD-16 | 双用户状态源 | P2 | App.vue / useUserInfo | 前端状态错乱 | 统一 Pinia/单 hook |
| TD-17 | openspec Agent 超前 | P3 | openspec | 预期管理 | 标注未实现 |
| TD-18 | README 过时 | P3 | README.md | 新人误导 | 重写为真实定位 |
| TD-19 | 无 CI/CD | P1 | 仓库 | 回归无门禁 | 最小 GitHub/Codeup 流水线 |
| TD-20 | 生产 SQL DEBUG | P1 | application.yml | 性能/泄露 | 按 profile 关闭 |

---

## 22. 重构 vs 重写决策

| 维度 | A 渐进重构 | B 模块化单体 | C 新旧并行 | D 整体重写 |
|------|------------|--------------|------------|------------|
| 风险 | 低-中 | 中 | 高 | **极高** |
| 成本 | 中 | 中高 | 高 | 极高 |
| 时间 | 可持续迭代 | 3–6 月量级 | 更长 | 更长且不确定 |
| 数据迁移 | 几乎无 | 包迁移为主 | 双写/迁移 | 全量迁移 |
| 业务影响 | 可控 | 可控 | 需双轨运营 | 停顿风险大 |
| 技术收益 | 高（债清） | 高（边界） | 中 | 理论高、落地差 |
| 可维护性 | 逐步升 | 明显升 | 过渡期更差 | 依赖执行质量 |

**评分建议（1–5，5 最好）：**

| 方案 | 综合 |
|------|-----:|
| A 渐进重构 | **4.5**（推荐主路径） |
| B 模块化单体 | **4.0**（A 的自然演进） |
| C 并行迁移 | 2.5（仅当出现第二套前端/客户隔离时） |
| D 重写 | **1.5**（库存一致性与页面债务不证明需要重写） |

**明确建议：选 A，并在 Phase 4 引入 B 的包级/Maven 模块边界；拒绝 D。**

理由：五层库存与锁库是业务护城河；问题是“实现膨胀与治理缺失”，不是“领域模型错误”。

---

## 23. 目标架构 V2

**适合度判断：模块化单体（Modular Monolith）> 普通三层整理 > DDD 战术全套 > 微服务。**

当前规模（~51k Java LOC + ~94k Vue LOC 量级、单团队）**不适合上微服务**。DDD 战略分域可借鉴 openspec，但不必上完整六边形/多 jar 爆炸。

```text
┌──────────────────────────── Frontend (Vue3) ────────────────────────────┐
│  ck-app（仓储） │ community-app（社区） │ shared（auth/net/ui-kit）        │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │ /api/*
┌──────────────────────────────────▼──────────────────────────────────────┐
│                         API (Controllers)                                │
│  auth │ warehouse │ community │ ai-content │ file                        │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────┐
│                      Application (Facades / UseCases)                    │
│  按用例拆分：InboundApprove / OutboundApprove / StockTakeInit ...         │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────┐
│ Domain                                                                   │
│  inventory（唯一库存变更入口）│ order │ masterdata │ tenant │ social      │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────┐
│ Infrastructure                                                           │
│  MyBatis │ Redis │ MQ │ COS │ DeepSeek │ Jimeng │ SMS │ Mail             │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │
                    MySQL / Redis / RabbitMQ / External APIs
```

原则：

- **库存变更只允许一个 Domain Inventory Gateway**（Holder 升正，Facade 只编排）。
- AI Agent Foundation 仅当真实需求出现再落地；不提前造 Runtime。
- 社区与仓储可同进程，包边界清晰，**共享 account/tenant**。

---

## 24. 重构 Roadmap

### Phase 0：建立安全基线

```text
目标：可重复构建、密钥脱库、测试/门禁骨架、API/DB 基线快照
涉及：配置、README、CI、测试脚手架、openspec 对齐
修改范围：非业务逻辑为主；可轮换密钥
风险：低（但密钥轮换需运维配合）
验收：dev 无私钥；prod 无空 JWT；CI 能 build front+back；核心 API 清单入库
可独立上线：是
```

### Phase 1：解决 P0

```text
目标：消除数据错误与安全致命项
涉及：JwtUtils、TestController、CkOutboundFacade.validateAndGetOrder、
      CkStockFacade.lockInventoryForStock、CkInboundFacade 库存入口统一、密钥
风险：中（触碰库存/出库，必须先有测试）
验收：非法状态审核失败；盘点锁行为明确；入库单一入口；Test 入口不可达；登出失效
可独立上线：建议分 PR，逐项上线
```

### Phase 2：拆巨型模块

```text
目标：Outbound/Product/Inventory/Inbound Facade 按用例拆分
涉及：Facade、少量 Controller 委托
风险：中高；需行为对照测试
验收：单文件 < ~500–800 行；公开方法行为快照一致
可独立上线：是（内部重构）
```

### Phase 3：统一公共能力

```text
目标：RestBean 错误码、BusinessException、HTTP Client、Redis、日志字段、权限基线
涉及：filter、advice、utils、config
风险：中（响应 message 变化影响前端）
验收：错误码文档；reqId+tenantId+userId 日志；Redis 用途单一文档化
可独立上线：是
```

### Phase 4：业务模块化

```text
目标：backend 按 warehouse/community/ai/file 分包或 Maven 子模块
涉及：包迁移、依赖方向约束
风险：中；合并冲突大
验收：warehouse 不依赖 quan 细节；编译边界检查
可独立上线：是（无行为变更）
```

### Phase 5：前端重构

```text
目标：拆 TOP10 页面；建立 api/ 层；Pinia 用户态；入库/出库模板化
涉及：views/ck、net、router
风险：中高（UI 回归）
验收：SalesOutboundCreate 拆分为容器+步骤组件；重复入库页合并参数化
可独立上线：按页面灰度
```

### Phase 6：基础设施升级

```text
目标：Docker Compose 本地一体；prod 配置净化；可选 XXL-JOB（锁超时/预警）
涉及：部署脚本、yml、job
风险：中
验收：一键起 MySQL/Redis/MQ/App；无明文默认密码
可独立上线：是
```

### Phase 7：测试与性能

```text
目标：核心链路自动化 + 库存查询 SQL 优化 + 压测
涉及：test、CkInventoryFacade、索引
风险：低-中
验收：TOP10 链路 CI 红线；pageList 大数据量 P95 达标
可独立上线：是
```

---

## 25. 暂缓重构清单（先别动）

| 模块 | 原因 | 建议 |
|------|------|------|
| `InventoryHolder` 核心扣加逻辑 | 无测试；五层一致性；事故面最大 | 先表征测试再动 |
| `CkInventoryLockService` 全量重写 | 1484 行；出库命脉；异步路径复杂 | 只修明确 bug，不“美化” |
| 生产环境库存历史数据迁移 | 无 schema 基线 | 先导出基线与对账脚本 |
| `ck_approval_*` 大改 | 与单据 status 双轨，业务规则不清 | 产品确认后再统一 |
| `ck_transfer_order` 强行启用/删除 | 半废弃，可能有历史数据 | 只读盘点使用率后再决策 |
| 社区 `ProjectFacade`/`TieFacade` 大拆 | 非当前仓储主线；ROI 低 | 排期靠后 |
| AI Agent Foundation 落地 | 仅 spec，无运行代码 | 有明确产品需求再开 |
| 全面 TypeScript 迁移 | 成本高、与渐进目标无关 | 新文件可选 TS，不整体搬 |
| 微服务拆分 | 团队/运维不匹配 | 明确拒绝现阶段 |
| MySQL 表 `redis` 改名 | 需停机/双写 | P2，单独窗口 |
| 即梦/DeepSeek 换厂商 | 强依赖现有封装 | 仅做 Client 适配层时再动 |

---

## 26. 附录：量化快照（2026-09-14）

| 指标 | 数值 |
|------|------|
| 后端 Java LOC（约） | 51,253 |
| 前端 Vue LOC（约） | 93,917 |
| Controller | ~31 |
| Facade | 30 |
| Service 接口+实现 | ~153 |
| Mapper | ~80 |
| `@TableName` 表 | ~91 |
| HTTP 映射 | POST 133 + GET 92 |
| `@Transactional` | ~46 / 16 files |
| 有效自动化测试 | ≈0 |
| 最大 Java 文件 | CkOutboundFacade 2644 |
| 最大 Vue 文件 | SalesOutboundCreate 6634 |

---

## 27. 下一步（本阶段之后，仍不自动开工）

1. 产品/负责人确认：**P0 清单优先级与上线窗口**。  
2. 导出一份 **API 清单 + 核心表 DDL 基线**（只读）。  
3. 先立 **Phase 0 CI + 密钥治理**，再碰库存代码。  
4. 任何 AI 编码必须遵守 `openspec/project.md`：复杂业务进 Facade、库存变更保五层一致、禁止密钥入库。

---

*本报告为只读审计产物，不构成已实施重构。与 `当前仓储系统架构分析报告.md` 冲突处以本报告“代码核实”结论为准（尤其 JWT 黑名单状态、MQ 实际同步路径、TestController 风险）。*
