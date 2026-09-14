# WMS Business Split Analysis

> 分支：`refactor/wms-ai-20260914`（基于 `feature-common-20251222`）  
> 日期：2026-09-14  
> 目的：在删除社区/圈子/聊天/成就代码前，完成业务归属与依赖矩阵。

---

## 1. 产品边界变更

| 删除 | 保留 |
|------|------|
| 副业社区 / 项目撮合 | 多租户 WMS 全链路 |
| 圈子 / 帖子 / 评论互动 | 用户登录 / JWT / 租户 |
| 私聊 IM / 站内社交消息 | 文件上传 / COS |
| 成就 / 积分 / 徽章 | AI Client 基础设施（DeepSeek / 即梦 SDK 壳） |
| 社区工具（取名、桃花、Toolbox） | Redis / Mail / 限流 / 操作日志 |

---

## 2. 分类原则

- **A**：纯社区/圈子/聊天/成就，WMS 零依赖 → 删除代码  
- **B**：共享基础设施或 WMS → 保留  
- **C**：双边依赖 → 先拆再删社区侧  

**数据库：本轮不 DROP 表**，仅输出待废弃清单。

---

## 3. A 类 — 确定删除（摘要）

### 后端 Controller
`AchievementController`, `ChatController`, `CommonController`, `MessageContorller`, `MyController`, `ProjectController`, `ProjectMemberController`, `QuanController`, `TestController`, `UnauthProjectController`, `UnauthQuanController`

### 后端 Facade
`ChatFacade`, `MessageFacade`, `MyFacade`, `ProjectFacade`, `ProjectMemberFacade`, `QuanFacade`, `TaskFacade`(成就), `TieFacade`

### 后端领域
全部 `Project*` / `Quan*` / `Chat*` / `Messages*` / `UserFollow*` / `TaskBadge*` / `TaskDefinition*` / `TaskUser*` / `Forum*` / `Images*` / `AccountShow*` / `PartnerLocations*` / `UserSkills*` / `UserFavorites*` / `AiRecommendLogs*`  
以及对应 Mapper/XML、req/resp、enums（Project/Quan/Tie/Chat/Message/Achievement）、`AutoPublish*` job、`DeepSeekContentService`、`AutoProjectService*`、`@TaskProgress`

### 前端页面
`IndexView`, `ProjectDetail`, `My`, `UserProfile`, `Quan*`, `Apply*`, `Application*`, `AdminApply*`, `CreateOfFindColleague`, `create-find-job`, `SkillMatch`, `TalentMatch`, `RankingList`, `Toolbox`, `NameGenerator`, `TaohuaView`, `GrowthCenter`, `UpdateUserInfo`, `MyMemberGroupDetail`, `ChatDialog`, `CommonActions`, `EmptyLayout`

### 表（仅标记废弃，不 DROP）
`projects*`, `project_*`, `quan_*`, `chat_*`, `messages`, `message_user_settings`, `forum_posts`, `task_badge`, `task_definition`, `task_user_*`, `user_follow`, `user_favorites`, `user_skills`, `account_show`, `partner_locations`, `ai_recommend_logs`

---

## 4. B 类 — 共享 / WMS 保留

| 类别 | 代表 |
|------|------|
| WMS | 全部 `controller/cangku/*`, `Facade/Ck*`, `CKProductFacade`, `CkCommentFacade`(货架分配), holder/*, ck_* entity |
| Auth | `AuthorizeController`, `UserController`, `UserFacade`, `Account*`, JWT filters |
| Tenant | `CkTenantController`, `CkTenantFacade`, `ck_tenant` |
| Upload | `UploadController`, `FsFacade`, COS config |
| AI Infra | `DeepSeekUtils`, `DeepSeekConfig`, `WebClientConfig`, `ChatRequest`, `DeepSeekResponse`, `JimengFacade`, `JMConfig`, `entity/jimeng/*` |
| Platform | Redis/Mail/Rabbit 配置, `RestBean`, LogAspect→`ck_operation_log`, 校验/异常 advice |
| 前端 | `views/ck/**`, welcome 登录注册, `net/index.js`, `useUserInfo`, WMS components |

**注意命名陷阱：**
- `TaskFacade` / `task_*` = 成就 → 删  
- `CkStockTakeTask*` / `CkProductionTask*` = WMS → 留  
- `MessageContorller` / `messages` = 社交站内信 → 删  
- `CkOperationLog*` = WMS 审计 → 留  
- `CkCommentFacade` = 货架库存分配 → 留  

---

## 5. C 类 — 歧义与处理

| 项 | 处理 |
|----|------|
| `CommonEnum.IndustryEnum` | **保留**文件（登录 JWT / UserController 仍用）；社区分类枚举随调用方删除自然无引用 |
| `AuthorizeController` `@TaskProgress` | 删注解引用 |
| `UploadController` → `ImagesMapper` | 移除无用注入 |
| `Config.WHITE_URL` | 去掉 `/api/unauth/project/**`, `/api/auth/project/simple` 等社区白名单 |
| `JimengFacade` | **保留**作 AI 图片基础设施；删除唯一社区调用方 AutoPublish/Test |
| `DeepSeekUtils` | **保留**；删除 `DeepSeekContentService`（社区文案） |
| `Account.industryCode` | 保留字段，不本轮改表 |
| `StockSnapResp` | 保留（WMS 盘点使用） |
| `WelcomeView` 文案 | 保留页面，后续改 WMS 品牌文案（本轮可轻改） |

---

## 6. 删除顺序

1. 写本分析文档  
2. 删后端 A 类（Controller → Facade → Service → Mapper → Entity）  
3. 修 C 类共享引用  
4. `mvn compile`  
5. 删前端页面 + 改 router/net/App  
6. `npm run build`  
7. 输出 SQL/Backlog/AI Proposal/Cleanup Report  

---

## 7. 本轮明确不做

- 不修审计 P0（库存双轨、出库校验、盘点锁、JWT 黑名单、密钥）  
- 不执行 DROP TABLE / 权限 SQL  
- 不 commit / push / 部署  
- 不大规模开发 AI Copilot  
