-- 2026-07-11 删除“提交作业”独立菜单，功能合并进“我的作业”
-- 说明：原“提交作业”(menu 754) 页面已删除，其提交/保存草稿/删除草稿功能改为在
-- “我的作业”(menu 753) 页面内以弹窗方式完成。故将相关按钮权限挂到 753 下，并删除菜单 754。

-- 1. 将按钮权限（保存草稿/提交作业/删除草稿）重新挂到“我的作业”下，并调整排序
UPDATE `sys_menu` SET `parent_id` = 753, `sort` = 2 WHERE `id` = 781; -- 保存草稿
UPDATE `sys_menu` SET `parent_id` = 753, `sort` = 3 WHERE `id` = 782; -- 提交作业
UPDATE `sys_menu` SET `parent_id` = 753, `sort` = 4 WHERE `id` = 783; -- 删除草稿

-- 2. 删除“提交作业”菜单本身及其角色关联
DELETE FROM `sys_role_menu` WHERE `menu_id` = 754;
DELETE FROM `sys_menu` WHERE `id` = 754;
