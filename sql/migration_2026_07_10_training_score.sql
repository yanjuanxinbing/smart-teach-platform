-- =====================================================================
-- 训练报名分数字段迁移脚本
-- 日期: 2026-07-10
-- 说明: 给已有 training_registration 表加平时成绩/考核成绩/占比字段
-- 用法: 已有数据的库直接 source 本文件即可；
--       新装环境不需要跑这个，init.sql 已经包含这些列
-- =====================================================================

USE `smart_teach_platform`;

-- 检查列是否已存在，存在则跳过（幂等）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                     AND table_name = 'training_registration'
                     AND column_name = 'regular_score');

SET @sql = IF(@col_exists = 0,
              'ALTER TABLE `training_registration`
                 ADD COLUMN `regular_score`  DECIMAL(5,2) DEFAULT NULL COMMENT ''平时成绩'' AFTER `sign_out_time`,
                 ADD COLUMN `exam_score`     DECIMAL(5,2) DEFAULT NULL COMMENT ''考核成绩'' AFTER `regular_score`,
                 ADD COLUMN `regular_weight` INT          DEFAULT NULL COMMENT ''平时成绩占比（百分比）'' AFTER `exam_score`,
                 ADD COLUMN `exam_weight`    INT          DEFAULT NULL COMMENT ''考核成绩占比（百分比）'' AFTER `regular_weight`',
              'SELECT ''training_registration.score columns already exist, skip''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
