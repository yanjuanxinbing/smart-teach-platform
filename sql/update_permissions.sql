-- =====================================================
-- 权限修复脚本：实训报名管理缺少权限按钮
-- =====================================================
-- 问题：前端报名管理提交时报错"没有相关权限"
-- 原因：数据库缺少实训报名的"新增"和"删除"权限按钮
-- 执行对象：所有团队成员
-- 执行时机：git pull 后发现报名管理权限问题时执行
-- =====================================================

-- 1. 清理可能存在的旧数据（避免主键冲突）
DELETE FROM sys_role_menu WHERE menu_id IN (320, 321, 322, 323);
DELETE FROM sys_menu WHERE id IN (320, 321, 322, 323);

-- 2. 插入新的权限按钮（报名管理的四个操作按钮）
INSERT INTO `sys_menu`(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort`, `visible`, `status`) VALUES
(320, 302, '新增报名', 3, NULL, NULL, NULL, 'training:registration:add', 1, 1, 1),
(321, 302, '审核报名', 3, NULL, NULL, NULL, 'training:registration:review', 2, 1, 1),
(322, 302, '登记成绩', 3, NULL, NULL, NULL, 'training:registration:grade', 3, 1, 1),
(323, 302, '删除报名', 3, NULL, NULL, NULL, 'training:registration:remove', 4, 1, 1);

-- 3. 清理可能冲突的权限关联记录
DELETE FROM sys_role_menu WHERE id >= 1020 AND id <= 1023;

-- 4. 为超级管理员角色分配新权限（角色ID=1，对应超级管理员）
INSERT INTO `sys_role_menu`(`id`, `role_id`, `menu_id`) VALUES
(1020, 1, 320),
(1021, 1, 321),
(1022, 1, 322),
(1023, 1, 323);

-- =====================================================
-- 验证执行结果（可选，用于检查是否成功）
-- =====================================================
-- 查看报名管理的权限按钮
SELECT * FROM sys_menu WHERE parent_id = 302 ORDER BY sort;

-- 查看超级管理员是否拥有这些权限
SELECT * FROM sys_role_menu WHERE role_id = 1 AND menu_id IN (320, 321, 322, 323);