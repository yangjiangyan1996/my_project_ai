# WMS Cleanup Report

> 执行分支：`refactor/wms-ai-20260914`  
> 源分支：`feature-common-20251222`  
> 日期：2026-09-14  
> 状态：**未 commit / 未 push / 未执行 DROP / 未部署**

---

## 1. 目标达成情况

| 目标 | 状态 |
|------|------|
| 删除副业社区 / 圈子 / 聊天 / 成就业务代码 | ✅ |
| 保留多租户 WMS 全链路 | ✅ |
| 保留登录 / 用户 / 租户 / 上传 | ✅ |
| 保留 AI Client 基础设施 | ✅ DeepSeekUtils + JimengFacade |
| 前后端可构建 | ✅ `mvn compile` (JDK17) / `npm run build` |
| 不修 WMS P0 | ✅ 仅记入 Backlog |
| 不 DROP 表 | ✅ 仅 SQL 注释清单 |

---

## 2. 删除范围摘要

### 后端（约 260+ Java 文件从版本库删除）

- Controllers：Project / Quan / Chat / Message / My / Achievement / Common / Test / UnauthProject / UnauthQuan  
- Facades：Project* / Quan / Tie / Chat / Message / My / Task(成就)  
- 对应 Service / Impl / Mapper / Entity / Req / VO / Enums / Jobs / `@TaskProgress`  
- 社区 AI 业务：`DeepSeekContentService`、`AutoProjectService*`  
- 配置：`auto-publish` 段移除；`WHITE_URL` 去掉社区路径  

### 前端

- 删除 21 个社区/工具页面 + ChatDialog / CommonActions / EmptyLayout  
- Router 仅保留 Welcome + `views/ck/**`  
- 全局守卫：未登录跳转登录（WMS SaaS）  
- Welcome 文案改为仓储定位  

---

## 3. 保留的 API 前缀（当前代码）

```text
/api/auth                 # login/register/reset
/api/auth/user
/api/auth/common/         # upload
/api/auth/tenant
/api/auth/warehouse/
/api/auth/shelf/
/api/auth/product
/api/auth/sku
/api/auth/unit/
/api/auth/supplier/
/api/auth/customer/
/api/auth/inbound/
/api/auth/outbound/
/api/auth/inventory
/api/auth/stock
/api/auth/adjust
/api/auth/adjust/transfer
/api/auth/recommend
/api/auth/operationLog/
/api/unauth/              # cangku UnauthController（WMS 侧，需后续审视）
```

**疑似非社区但仍需关注：** `/api/unauth/`（cangku 包内）— 本轮保留，记入 Backlog 审视。

**已不存在：** `/api/auth/project` `/api/auth/quan` `/api/auth/chat` `/api/auth/msg` `/api/auth/achievement` `/api/auth/my`

---

## 4. 共享模块处理（C 类）

| 项 | 处理 |
|----|------|
| UploadController × ImagesMapper | 移除注入，保留上传 |
| AuthorizeController × TaskProgress | 去掉注解 |
| Config.WHITE_URL | 去社区白名单 |
| DeepSeek / Jimeng | 保留基础设施 |
| IndustryEnum | 保留（JWT） |

---

## 5. 构建结果

| 项 | 结果 |
|----|------|
| Backend `mvn clean compile` (Temurin 17) | **BUILD SUCCESS** |
| Backend `mvn test` | FAIL：`contextLoads` 无 DataSource（环境既有问题，非删除引入） |
| Frontend `npm run build` | **SUCCESS** |
| JDK21 直接编译 | Lombok 不兼容（既有环境问题）；需 JDK17 |

---

## 6. 残留关键词扫描

对 `圈子|副业|QuanList|ProjectFacade|Achievement|ChatDialog` 等扫描：

- 后端 Java：无业务残留  
- 前端 Vue/JS：无路由/页面残留（Welcome 已改文案）  
- `openspec/specs/project-community` 等文档仍在 → 文档债，未删  

---

## 7. Git Diff 审计结论

- **309 files changed，约 -36k / +1.2k 行**（相对 HEAD；含此前未提交的 WMS 调整单改动）  
- 版本库标记删除文件约 **288**  
- WMS 核心 Facade / SalesOutbound / CkIndex / DeepSeekUtils / JimengFacade **均在**  
- 未发现误删 `CkOutboundFacade` / `InventoryHolder` / `ck_*` Entity  

---

## 8. 交付文件清单

| 文件 | 用途 |
|------|------|
| `WMS_BUSINESS_SPLIT_ANALYSIS.md` | 删除前归属分析 |
| `WMS_DEPRECATED_TABLES.sql` | 待审 DROP 清单（注释） |
| `WMS_PERMISSION_CLEANUP.sql` | 待审权限清理 |
| `WMS_REFACTOR_BACKLOG.md` | P0 与后续债 |
| `AI_WMS_ARCHITECTURE_PROPOSAL.md` | AI WMS 规划 |
| `WMS_CLEANUP_REPORT.md` | 本报告 |
| `PROJECT_REFACTOR_AUDIT.md` | 前期审计（只读） |

---

## 9. 请审核人重点抽查

1. 登录 → 进入 `/` CkIndex  
2. 商品 / 仓库 / 入库 / 出库 / 库存 / 盘点页面可打开  
3. 社区 URL 404  
4. 后端无 `/api/auth/quan` 等映射  
5. 确认不执行本目录下 SQL  
