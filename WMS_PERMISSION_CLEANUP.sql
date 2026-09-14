-- WMS_PERMISSION_CLEANUP.sql
-- 生成日期: 2026-09-14
-- 分支: refactor/wms-ai-20260914
-- 说明: 待审核。禁止直接在生产执行。
-- 背景: 仓储 RBAC 表 (ck_menu/ck_role/ck_role_menu/ck_user_role) 存在但未接入鉴权；
--       社区路由权限主要在前端 meta.requiresAuth，无独立菜单表行可核对。
--       以下为「前端路由/权限标识」清理对照，以及建议的库内清理模板。

-- ========== 前端已删除的社区入口（代码层已清理） ==========
-- /views/IndexView
-- /index/detail/:id
-- /index/my*
-- /index/quan/*
-- /index/QuanList
-- /index/Toolbox
-- /index/talentMatch
-- /index/rankingList
-- /SkillMatch.vue
-- /name-generator
-- /taohua
-- GrowthCenter（原本未挂路由）

-- ========== 建议清理的权限标识（若 ck_menu.path/code 曾写入社区路径） ==========
-- 待确认（示例，需先 SELECT 核对真实数据）:
-- SELECT id, code, name, path FROM ck_menu
-- WHERE path LIKE '%Quan%'
--    OR path LIKE '%quan%'
--    OR path LIKE '%IndexView%'
--    OR path LIKE '%Growth%'
--    OR path LIKE '%chat%'
--    OR path LIKE '%achievement%'
--    OR name LIKE '%圈子%'
--    OR name LIKE '%副业%'
--    OR name LIKE '%成就%'
--    OR name LIKE '%聊天%';

-- 待确认:
-- DELETE FROM ck_role_menu WHERE menu_id IN (...上述菜单 id...);
-- DELETE FROM ck_menu WHERE id IN (...);

-- ========== account.role 字符串 ==========
-- 社区 ADMIN 审批等逻辑已随 Controller 删除。
-- WMS 仍可能使用 account.role；本轮不修改角色数据。

-- ========== 白名单已清理（代码） ==========
-- Config.WHITE_URL 已移除:
--   /api/auth/project/simple
--   /api/unauth/project/**
-- 保留: /api/auth/login|register|ask-code|common/**|/api/unauth/**

-- 注意: 本轮未执行任何 DML/DDL。
