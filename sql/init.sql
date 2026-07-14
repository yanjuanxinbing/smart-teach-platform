-- =====================================================================
-- 智能化在线教学支持服务平台管理中心系统 - 数据库初始化脚本
-- MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================================

-- 门户轮播图封面使用 dataURL(base64) 存储，单条记录可能超过 MySQL 默认 max_allowed_packet=4MB
-- 这里调整到 16MB（需要 SUPER 权限）。生产环境也可以写到 my.cnf 的 [mysqld] 段：
--   [mysqld]
--   max_allowed_packet = 16M
SET GLOBAL max_allowed_packet = 16777216;

CREATE DATABASE IF NOT EXISTS `smart_teach_platform`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `smart_teach_platform`;

-- 强制连接字符集，避免 Windows 下 mysql 客户端默认按 GBK 读文件导致中文 INSERT 失败
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 通用：逻辑删除标记、创建/更新时间、雪花ID
-- ---------------------------------------------------------------------

-- ---------------------------------------------------------------------
-- 1. 用户/权限 5张表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL COMMENT '主键，雪花ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    `real_name`   VARCHAR(50)           DEFAULT NULL COMMENT '姓名',
    `phone`       VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `email`       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `avatar`      VARCHAR(255)          DEFAULT NULL COMMENT '头像',
    `gender`      TINYINT               DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `dept_id`     BIGINT                DEFAULT NULL COMMENT '所属部门ID',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `remark`      VARCHAR(500)          DEFAULT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT                DEFAULT NULL,
    `update_by`   BIGINT                DEFAULT NULL,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    -- 仅对"未删除"的行强制唯一；逻辑删除（deleted=1）的行 username 可重复，
    -- 这样删除用户后可重新创建同名/同账号用户。NULL 在函数式索引中不参与唯一性判断。
    UNIQUE KEY `uk_username_active` ((CASE WHEN deleted = 0 THEN username END))
) ENGINE = InnoDB COMMENT ='系统用户';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT      NOT NULL,
    `role_name`   VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(50) NOT NULL COMMENT '角色编码',
    `sort`        INT                  DEFAULT 0,
    `status`      TINYINT     NOT NULL DEFAULT 1,
    `remark`      VARCHAR(500)         DEFAULT NULL,
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT               DEFAULT NULL,
    `update_by`   BIGINT               DEFAULT NULL,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB COMMENT ='角色';

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id`          BIGINT       NOT NULL,
    `parent_id`   BIGINT       NOT NULL DEFAULT 0,
    `menu_name`   VARCHAR(50)  NOT NULL,
    `menu_type`   TINYINT      NOT NULL COMMENT '1目录 2菜单 3按钮',
    `path`        VARCHAR(200)          DEFAULT NULL,
    `component`   VARCHAR(200)          DEFAULT NULL,
    `icon`        VARCHAR(50)           DEFAULT NULL,
    `permission`  VARCHAR(100)          DEFAULT NULL,
    `sort`        INT                  DEFAULT 0,
    `visible`     TINYINT               DEFAULT 1 COMMENT '0隐藏 1显示',
    `status`      TINYINT      NOT NULL DEFAULT 1,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT                DEFAULT NULL,
    `update_by`   BIGINT                DEFAULT NULL,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='菜单/权限';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`      BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_role` (`role_id`)
) ENGINE = InnoDB COMMENT ='用户-角色关系';

DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `id`      BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    `menu_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_role` (`role_id`),
    KEY `idx_menu` (`menu_id`)
) ENGINE = InnoDB COMMENT ='角色-菜单关系';

-- ---------------------------------------------------------------------
-- 2. 部门/字典/参数配置
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
    `id`          BIGINT      NOT NULL,
    `parent_id`   BIGINT      NOT NULL DEFAULT 0,
    `dept_name`   VARCHAR(50) NOT NULL,
    `dept_code`   VARCHAR(50)          DEFAULT NULL,
    `sort`        INT                  DEFAULT 0,
    `leader`      VARCHAR(50)          DEFAULT NULL,
    `phone`       VARCHAR(20)          DEFAULT NULL,
    `email`       VARCHAR(100)         DEFAULT NULL,
    `status`      TINYINT     NOT NULL DEFAULT 1,
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT               DEFAULT NULL,
    `update_by`   BIGINT               DEFAULT NULL,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='部门';

DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
    `id`          BIGINT      NOT NULL,
    `dict_name`   VARCHAR(100) NOT NULL,
    `dict_type`   VARCHAR(100) NOT NULL,
    `description` VARCHAR(500)         DEFAULT NULL,
    `status`      TINYINT     NOT NULL DEFAULT 1,
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT               DEFAULT NULL,
    `update_by`   BIGINT               DEFAULT NULL,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE = InnoDB COMMENT ='字典类型';

DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
    `id`          BIGINT      NOT NULL,
    `dict_type`   VARCHAR(100) NOT NULL,
    `dict_label`  VARCHAR(100) NOT NULL,
    `dict_value`  VARCHAR(100) NOT NULL,
    `css_class`   VARCHAR(50)          DEFAULT NULL,
    `list_class`  VARCHAR(50)          DEFAULT NULL,
    `sort`        INT                  DEFAULT 0,
    `status`      TINYINT     NOT NULL DEFAULT 1,
    `is_default`  TINYINT     NOT NULL DEFAULT 0,
    `remark`      VARCHAR(500)         DEFAULT NULL,
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT               DEFAULT NULL,
    `update_by`   BIGINT               DEFAULT NULL,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dict_type` (`dict_type`)
) ENGINE = InnoDB COMMENT ='字典数据';

DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id`          BIGINT      NOT NULL,
    `config_name` VARCHAR(100) NOT NULL,
    `config_key`  VARCHAR(100) NOT NULL,
    `config_value` VARCHAR(500)        DEFAULT NULL,
    `config_type` TINYINT     NOT NULL DEFAULT 0 COMMENT '0业务 1系统内置',
    `remark`      VARCHAR(500)         DEFAULT NULL,
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT               DEFAULT NULL,
    `update_by`   BIGINT               DEFAULT NULL,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE = InnoDB COMMENT ='系统参数';

-- ---------------------------------------------------------------------
-- 3. 课程计划模块 5张表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id`           BIGINT       NOT NULL,
    `course_code`  VARCHAR(50)  NOT NULL,
    `course_name`  VARCHAR(100) NOT NULL,
    `category_id`  BIGINT                DEFAULT NULL,
    `category_name` VARCHAR(50)          DEFAULT NULL,
    `description`  TEXT,
    `cover_image`  VARCHAR(255)          DEFAULT NULL,
    `teacher_id`   BIGINT                DEFAULT NULL,
    `teacher_name` VARCHAR(50)           DEFAULT NULL,
    `credit`       DECIMAL(4, 1)         DEFAULT NULL,
    `total_hours`  INT                  DEFAULT NULL,
    `course_type`  TINYINT               DEFAULT 1 COMMENT '1必修 2选修',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0未发布 1已发布 2已结课',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    BIGINT                DEFAULT NULL,
    `update_by`    BIGINT                DEFAULT NULL,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_code` (`course_code`)
) ENGINE = InnoDB COMMENT ='课程';

-- 课程-教师授课关系（多对多；新模块「授课管理」维护此表）
DROP TABLE IF EXISTS `course_teacher`;
CREATE TABLE `course_teacher` (
    `id`          BIGINT       NOT NULL,
    `course_id`   BIGINT       NOT NULL                COMMENT '课程ID',
    `teacher_id`  BIGINT       NOT NULL                COMMENT '教师用户ID',
    `role`        VARCHAR(20)           DEFAULT '主讲' COMMENT '主讲/助教',
    `sort`        INT                   DEFAULT 1,
    `status`      TINYINT      NOT NULL DEFAULT 1       COMMENT '0禁用 1启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT                DEFAULT NULL,
    `update_by`   BIGINT                DEFAULT NULL,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_course_teacher` (`course_id`, `teacher_id`),
    KEY `idx_teacher_course` (`teacher_id`, `course_id`)
) ENGINE = InnoDB COMMENT ='课程-教师授课关系';

DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter` (
    `id`            BIGINT      NOT NULL,
    `course_id`     BIGINT      NOT NULL,
    `parent_id`     BIGINT      NOT NULL DEFAULT 0,
    `chapter_title` VARCHAR(200) NOT NULL,
    `sort`          INT                  DEFAULT 0,
    `hours`         INT                  DEFAULT NULL,
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT               DEFAULT NULL,
    `update_by`     BIGINT               DEFAULT NULL,
    `deleted`       TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_course` (`course_id`)
) ENGINE = InnoDB COMMENT ='课程章节';

DROP TABLE IF EXISTS `course_content`;
CREATE TABLE `course_content` (
    `id`            BIGINT       NOT NULL,
    `course_id`     BIGINT       NOT NULL,
    `chapter_id`    BIGINT       NOT NULL,
    `content_title` VARCHAR(200) NOT NULL,
    `content_type`  TINYINT      NOT NULL COMMENT '1PPT 2视频 3文档 4富文本',
    `resource_id`   BIGINT                DEFAULT NULL,
    `resource_url`  VARCHAR(500)         DEFAULT NULL,
    `rich_text`     MEDIUMTEXT,
    `sort`          INT                  DEFAULT 0,
    `hours`         INT                  DEFAULT NULL,
    `status`        TINYINT      NOT NULL DEFAULT 1,
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT                DEFAULT NULL,
    `update_by`     BIGINT                DEFAULT NULL,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_course` (`course_id`),
    KEY `idx_chapter` (`chapter_id`)
) ENGINE = InnoDB COMMENT ='课程内容';

DROP TABLE IF EXISTS `course_plan`;
CREATE TABLE `course_plan` (
    `id`             BIGINT       NOT NULL,
    `plan_title`     VARCHAR(200) NOT NULL,
    `course_id`      BIGINT       NOT NULL,
    `course_name`    VARCHAR(100)          DEFAULT NULL,
    `semester`       VARCHAR(20)           DEFAULT NULL,
    `class_name`     VARCHAR(50)           DEFAULT NULL,
    `start_date`     DATE                  DEFAULT NULL,
    `end_date`       DATE                  DEFAULT NULL,
    `total_weeks`    INT                  DEFAULT NULL,
    `description`    TEXT,
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已完成 3驳回',
    `approver_id`    BIGINT                DEFAULT NULL,
    `approver_name`  VARCHAR(50)           DEFAULT NULL,
    `approve_remark` VARCHAR(500)          DEFAULT NULL,
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      BIGINT                DEFAULT NULL,
    `update_by`      BIGINT                DEFAULT NULL,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='课程计划';

DROP TABLE IF EXISTS `course_plan_item`;
CREATE TABLE `course_plan_item` (
    `id`            BIGINT NOT NULL,
    `plan_id`       BIGINT NOT NULL,
    `week_no`       INT,
    `chapter_id`    BIGINT          DEFAULT NULL,
    `chapter_title` VARCHAR(200)    DEFAULT NULL,
    `content`       TEXT,
    `objective`     VARCHAR(500)    DEFAULT NULL,
    `method`        VARCHAR(500)    DEFAULT NULL,
    `hours`         INT             DEFAULT NULL,
    `remark`        VARCHAR(500)    DEFAULT NULL,
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT          DEFAULT NULL,
    `update_by`     BIGINT          DEFAULT NULL,
    `deleted`       TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_plan` (`plan_id`)
) ENGINE = InnoDB COMMENT ='课程计划明细';

-- ---------------------------------------------------------------------
-- 4. 课程实验计划模块 2张表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `experiment_plan`;
CREATE TABLE `experiment_plan` (
    `id`              BIGINT       NOT NULL,
    `plan_title`      VARCHAR(200) NOT NULL,
    `course_id`       BIGINT                DEFAULT NULL,
    `course_name`     VARCHAR(100)          DEFAULT NULL,
    `semester`        VARCHAR(20)           DEFAULT NULL,
    `class_name`      VARCHAR(50)           DEFAULT NULL,
    `teacher_id`      BIGINT                DEFAULT NULL,
    `teacher_name`    VARCHAR(50)           DEFAULT NULL,
    `lab_room`        VARCHAR(100)          DEFAULT NULL,
    `start_date`      DATE                  DEFAULT NULL,
    `end_date`        DATE                  DEFAULT NULL,
    `total_experiments` INT                DEFAULT NULL,
    `total_hours`     INT                  DEFAULT NULL,
    `description`     TEXT,
    `status`          TINYINT      NOT NULL DEFAULT 0,
    `approver_id`     BIGINT                DEFAULT NULL,
    `approver_name`   VARCHAR(50)           DEFAULT NULL,
    `approve_remark`  VARCHAR(500)          DEFAULT NULL,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`       BIGINT                DEFAULT NULL,
    `update_by`       BIGINT                DEFAULT NULL,
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='实验计划';

DROP TABLE IF EXISTS `experiment_plan_item`;
CREATE TABLE `experiment_plan_item` (
    `id`            BIGINT NOT NULL,
    `plan_id`       BIGINT NOT NULL,
    `exp_no`        INT,
    `exp_name`      VARCHAR(200)    DEFAULT NULL,
    `exp_type`      TINYINT         DEFAULT NULL COMMENT '1验证 2综合 3设计 4创新',
    `purpose`       TEXT,
    `content`       TEXT,
    `requirement`   TEXT,
    `resource_id`   BIGINT          DEFAULT NULL,
    `class_date`    DATE            DEFAULT NULL,
    `class_period`  VARCHAR(20)     DEFAULT NULL,
    `hours`         INT             DEFAULT NULL,
    `teacher_name`  VARCHAR(50)     DEFAULT NULL,
    `remark`        VARCHAR(500)    DEFAULT NULL,
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT          DEFAULT NULL,
    `update_by`     BIGINT          DEFAULT NULL,
    `deleted`       TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_plan` (`plan_id`)
) ENGINE = InnoDB COMMENT ='实验计划明细';

-- ---------------------------------------------------------------------
-- 5. 实训计划模块 2张表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `training_plan`;
CREATE TABLE `training_plan` (
    `id`             BIGINT       NOT NULL,
    `plan_title`     VARCHAR(200) NOT NULL,
    `project_name`   VARCHAR(200) NOT NULL,
    `course_id`      BIGINT                DEFAULT NULL,
    `course_name`    VARCHAR(100)          DEFAULT NULL,
    `semester`       VARCHAR(20)           DEFAULT NULL,
    `class_name`     VARCHAR(50)           DEFAULT NULL,
    `teacher_id`     BIGINT                DEFAULT NULL,
    `teacher_name`   VARCHAR(50)           DEFAULT NULL,
    `location`       VARCHAR(100)          DEFAULT NULL,
    `start_date`     DATE                  DEFAULT NULL,
    `end_date`       DATE                  DEFAULT NULL,
    `duration_days`  INT                  DEFAULT NULL,
    `total_hours`    INT                  DEFAULT NULL,
    `capacity`       INT                  DEFAULT NULL,
    `objective`      TEXT,
    `content`        TEXT,
    `assessment`     VARCHAR(500)          DEFAULT NULL,
    `status`         TINYINT      NOT NULL DEFAULT 0,
    `approver_id`    BIGINT                DEFAULT NULL,
    `approver_name`  VARCHAR(50)           DEFAULT NULL,
    `approve_remark` VARCHAR(500)          DEFAULT NULL,
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      BIGINT                DEFAULT NULL,
    `update_by`      BIGINT                DEFAULT NULL,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='实训计划';

DROP TABLE IF EXISTS `training_registration`;
CREATE TABLE `training_registration` (
    `id`              BIGINT NOT NULL,
    `plan_id`         BIGINT NOT NULL,
    `plan_title`      VARCHAR(200)    DEFAULT NULL,
    `student_id`      BIGINT          DEFAULT NULL,
    `student_name`    VARCHAR(50)     DEFAULT NULL,
    `class_name`      VARCHAR(50)     DEFAULT NULL,
    `phone`           VARCHAR(20)     DEFAULT NULL,
    `status`          TINYINT         DEFAULT 0 COMMENT '0待审核 1已通过 2已驳回 3已完成',
    `sign_in_time`    DATETIME        DEFAULT NULL,
    `sign_out_time`   DATETIME        DEFAULT NULL,
    `regular_score`   DECIMAL(5, 2)   DEFAULT NULL COMMENT '平时成绩',
    `exam_score`      DECIMAL(5, 2)   DEFAULT NULL COMMENT '考核成绩',
    `regular_weight`  INT             DEFAULT NULL COMMENT '平时成绩占比（百分比 0-100）',
    `exam_weight`     INT             DEFAULT NULL COMMENT '考核成绩占比（百分比 0-100）',
    `score`           DECIMAL(5, 2)   DEFAULT NULL COMMENT '最终成绩（按权重计算）',
    `comment`         VARCHAR(500)    DEFAULT NULL,
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`       BIGINT          DEFAULT NULL,
    `update_by`       BIGINT          DEFAULT NULL,
    `deleted`         TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_plan` (`plan_id`)
) ENGINE = InnoDB COMMENT ='实训报名';

-- ---------------------------------------------------------------------
-- 6. 资源管理模块 2张表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `resource_category`;
CREATE TABLE `resource_category` (
    `id`            BIGINT NOT NULL,
    `parent_id`     BIGINT NOT NULL DEFAULT 0,
    `category_name` VARCHAR(50) NOT NULL,
    `category_code` VARCHAR(50) DEFAULT NULL,
    `sort`          INT        DEFAULT 0,
    `icon`          VARCHAR(50) DEFAULT NULL,
    `description`   VARCHAR(500) DEFAULT NULL,
    `status`        TINYINT NOT NULL DEFAULT 1,
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT DEFAULT NULL,
    `update_by`     BIGINT DEFAULT NULL,
    `deleted`       TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='资源分类';

DROP TABLE IF EXISTS `biz_resource`;
CREATE TABLE `biz_resource` (
    `id`            BIGINT NOT NULL,
    `resource_name` VARCHAR(200) NOT NULL,
    `resource_type` TINYINT NOT NULL COMMENT '1文档 2图片 3视频 4音频 5压缩包 6其他',
    `category_id`   BIGINT DEFAULT NULL,
    `category_name` VARCHAR(50) DEFAULT NULL,
    `course_id`     BIGINT DEFAULT NULL,
    `course_name`   VARCHAR(100) DEFAULT NULL,
    `original_name` VARCHAR(255) DEFAULT NULL,
    `file_path`     VARCHAR(500) DEFAULT NULL,
    `file_url`      VARCHAR(500) DEFAULT NULL,
    `file_suffix`   VARCHAR(20) DEFAULT NULL,
    `file_size`     BIGINT DEFAULT NULL,
    `content_type`  VARCHAR(100) DEFAULT NULL,
    `tags`          VARCHAR(500) DEFAULT NULL,
    `description`   TEXT,
    `download_count` INT DEFAULT 0,
    `view_count`    INT DEFAULT 0,
    `status`        TINYINT NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
    `upload_by`     BIGINT DEFAULT NULL,
    `upload_name`   VARCHAR(50) DEFAULT NULL,
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT DEFAULT NULL,
    `update_by`     BIGINT DEFAULT NULL,
    `deleted`       TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='教学资源';

-- ---------------------------------------------------------------------
-- 7. 网站门户 1张表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `portal_article`;
CREATE TABLE `portal_article` (
    `id`            BIGINT NOT NULL,
    `type`          TINYINT NOT NULL COMMENT '1轮播图 2通知公告 3新闻资讯',
    `title`         VARCHAR(200) NOT NULL,
    `cover_image`   MEDIUMTEXT COMMENT '封面图，存 dataURL(base64) 或可访问URL',
    `link_url`      VARCHAR(500) DEFAULT NULL,
    `content`       MEDIUMTEXT,
    `sort`          INT DEFAULT 0,
    `top`           TINYINT DEFAULT 0,
    `publish_time`  DATETIME DEFAULT NULL,
    `status`        TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已下线',
    `view_count`    BIGINT DEFAULT 0,
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT DEFAULT NULL,
    `update_by`     BIGINT DEFAULT NULL,
    `deleted`       TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='门户内容';

-- ---------------------------------------------------------------------
-- 8. 系统监控 2张表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
    `id`         BIGINT NOT NULL,
    `username`   VARCHAR(50)  DEFAULT NULL,
    `ip`         VARCHAR(50)  DEFAULT NULL,
    `location`   VARCHAR(100) DEFAULT NULL,
    `browser`    VARCHAR(50)  DEFAULT NULL,
    `os`         VARCHAR(50)  DEFAULT NULL,
    `status`     TINYINT      DEFAULT NULL COMMENT '0失败 1成功',
    `message`    VARCHAR(255) DEFAULT NULL,
    `login_time` DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_login_time` (`login_time`)
) ENGINE = InnoDB COMMENT ='登录日志';

DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id`            BIGINT NOT NULL,
    `module`        VARCHAR(50)  DEFAULT NULL,
    `action`        VARCHAR(100) DEFAULT NULL,
    `method`        VARCHAR(200) DEFAULT NULL,
    `request_uri`   VARCHAR(200) DEFAULT NULL,
    `http_method`   VARCHAR(10)  DEFAULT NULL,
    `params`        TEXT,
    `result`        TEXT,
    `ip`            VARCHAR(50)  DEFAULT NULL,
    `user_id`       BIGINT       DEFAULT NULL,
    `username`      VARCHAR(50)  DEFAULT NULL,
    `status`        TINYINT      DEFAULT 1,
    `error_msg`     VARCHAR(2000) DEFAULT NULL,
    `cost_time`     BIGINT       DEFAULT NULL,
    `operation_time` DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_time` (`operation_time`)
) ENGINE = InnoDB COMMENT ='操作日志';

-- =====================================================================
-- 8. 作业管理模块 2 张表
-- =====================================================================
DROP TABLE IF EXISTS `assignment`;
CREATE TABLE `assignment` (
    `id`           BIGINT       NOT NULL,
    `course_id`    BIGINT       NOT NULL,
    `chapter_id`   BIGINT       NOT NULL,
    `content_id`   BIGINT                                         DEFAULT NULL COMMENT '关联 course_content.id（可选）',
    `title`        VARCHAR(200) NOT NULL,
    `description`  TEXT                                            DEFAULT NULL,
    `deadline`     DATETIME     NOT NULL,
    `total_score`  DECIMAL(5,2) NOT NULL                          DEFAULT 100.00,
    `allow_late`   TINYINT      NOT NULL                          DEFAULT 1 COMMENT '0否 1是',
    `status`       TINYINT      NOT NULL                          DEFAULT 0 COMMENT '0草稿 1已发布 2已截止',
    `create_time`  DATETIME     NOT NULL                          DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL                          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    BIGINT                                         DEFAULT NULL,
    `update_by`    BIGINT                                         DEFAULT NULL,
    `deleted`      TINYINT      NOT NULL                          DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_content`         (`content_id`),
    KEY `idx_course_chapter`  (`course_id`, `chapter_id`),
    KEY `idx_status`          (`status`),
    KEY `idx_deadline`        (`deadline`)
) ENGINE = InnoDB COMMENT ='作业';

DROP TABLE IF EXISTS `assignment_submission`;
CREATE TABLE `assignment_submission` (
    `id`             BIGINT        NOT NULL,
    `assignment_id`  BIGINT        NOT NULL,
    `student_id`     BIGINT        NOT NULL,
    `student_name`   VARCHAR(50)                                  DEFAULT NULL,
    `class_name`     VARCHAR(50)                                  DEFAULT NULL,
    `submit_text`    TEXT                                          DEFAULT NULL,
    `file_url`       VARCHAR(500)                                 DEFAULT NULL,
    `original_name`  VARCHAR(255)                                 DEFAULT NULL,
    `file_suffix`    VARCHAR(20)                                  DEFAULT NULL,
    `file_size`      BIGINT                                       DEFAULT NULL,
    `is_late`        TINYINT      NOT NULL                        DEFAULT 0 COMMENT '0否 1是',
    `submit_time`    DATETIME                                      DEFAULT NULL,
    `score`          DECIMAL(5,2)                                 DEFAULT NULL,
    `comment`        VARCHAR(500)                                 DEFAULT NULL,
    `grader_id`      BIGINT                                       DEFAULT NULL,
    `grader_name`    VARCHAR(50)                                  DEFAULT NULL,
    `grade_time`     DATETIME                                      DEFAULT NULL,
    `status`         TINYINT      NOT NULL                        DEFAULT 0 COMMENT '0草稿 1已提交 2已批改',
    `create_time`    DATETIME     NOT NULL                        DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      BIGINT                                        DEFAULT NULL,
    `update_by`      BIGINT                                        DEFAULT NULL,
    `deleted`        TINYINT      NOT NULL                        DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_assignment`             (`assignment_id`),
    KEY `idx_student`                (`student_id`),
    KEY `idx_assignment_student_time`(`assignment_id`, `student_id`, `submit_time`),
    KEY `idx_status`                 (`status`)
) ENGINE = InnoDB COMMENT ='作业提交';

-- =====================================================================
-- 9. 班级管理 3 张表
-- =====================================================================
DROP TABLE IF EXISTS `sys_class`;
CREATE TABLE `sys_class` (
    `id`          BIGINT       NOT NULL COMMENT '主键，雪花ID',
    `class_name`  VARCHAR(50)  NOT NULL                COMMENT '班级名称（如 计科2201班）',
    `grade`       VARCHAR(20)           DEFAULT NULL   COMMENT '年级（如 2022级）',
    `dept_id`     BIGINT       NOT NULL                COMMENT '所属部门ID',
    `sort`        INT                   DEFAULT 0,
    `status`      TINYINT      NOT NULL DEFAULT 1       COMMENT '0禁用 1启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT                DEFAULT NULL,
    `update_by`   BIGINT                DEFAULT NULL,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dept` (`dept_id`),
    KEY `idx_name` (`class_name`),
    -- 同一部门下不允许重名；含 deleted 是为了与"逻辑删除"配合：
    -- 同一 (dept_id, class_name) 可有 deleted=0 与 deleted=1 各 1 条，确保同名重建可走通
    UNIQUE KEY `uk_dept_class` (`dept_id`, `class_name`, `deleted`)
) ENGINE = InnoDB COMMENT ='班级';

DROP TABLE IF EXISTS `sys_user_class`;
CREATE TABLE `sys_user_class` (
    `id`          BIGINT   NOT NULL,
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `class_id`    BIGINT   NOT NULL                COMMENT '班级ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT            DEFAULT NULL,
    `update_by`   BIGINT            DEFAULT NULL,
    `deleted`     TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_class_user` (`class_id`, `user_id`),
    KEY `idx_user_class` (`user_id`, `class_id`)
) ENGINE = InnoDB COMMENT ='用户-班级关系';

DROP TABLE IF EXISTS `assignment_target_class`;
CREATE TABLE `assignment_target_class` (
    `id`            BIGINT   NOT NULL,
    `assignment_id` BIGINT   NOT NULL                COMMENT '作业ID',
    `class_id`      BIGINT   NOT NULL                COMMENT '班级ID',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT            DEFAULT NULL,
    `update_by`     BIGINT            DEFAULT NULL,
    `deleted`       TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_class_assignment` (`class_id`, `assignment_id`),
    KEY `idx_assignment_class` (`assignment_id`, `class_id`)
) ENGINE = InnoDB COMMENT ='作业-班级关系';

-- =====================================================================
-- 初始化数据
-- =====================================================================

-- 超级管理员账户
-- 密码：admin（BCrypt 哈希，必须以 $2a$/$2b$/$2y$ 开头，长度 60）
-- 这个哈希是 Spring Security BCryptPasswordEncoder.encode("admin") 的标准输出
-- 已通过 Spring Security 5.7.x / Spring Boot 2.7.18 反复验证可登录
INSERT INTO `sys_user`(`id`, `username`, `password`, `real_name`, `status`, `remark`)
VALUES (1, 'admin', '$2a$10$5gh9c1hWstKlLyz/xox/euvZjti8bsTkwUFa9VE5LCdMotl49FrFC', '超级管理员', 1, '系统内置账号');

-- 如果上面的哈希在你的 Spring Security 版本下还是不能登录，
-- 在项目根目录执行 mvn -q exec:java -Dexec.mainClass=com.smartteach.tools.PasswordHashGen -Dexec.args="admin"
-- 把输出的哈希值替换上面 INSERT 语句里的 password 字段，然后重新跑 init.sql 即可

-- 角色
INSERT INTO `sys_role`(`id`, `role_name`, `role_code`, `sort`, `status`) VALUES
(1, '超级管理员', 'ROLE_ADMIN', 1, 1),
(2, '系统管理员',  'ROLE_SYSTEM', 2, 1),
(3, '教师',        'ROLE_TEACHER', 3, 1),
(4, '学生',        'ROLE_STUDENT', 4, 1);

-- 管理员关联超级管理员角色
INSERT INTO `sys_user_role`(`id`, `user_id`, `role_id`) VALUES (1, 1, 1);

-- 菜单（基于系统所有功能模块）
INSERT INTO `sys_menu`(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort`, `visible`, `status`) VALUES
-- 顶级目录
(1,    0, '网站门户',   1, '/portal',     NULL,                    'promotion', '', 1, 1, 1),
(100,  0, '课程计划管理',     1, '/course',     NULL, 'Reading',       '', 2, 1, 1),
(200,  0, '课程实验计划管理', 1, '/experiment', NULL, 'DataAnalysis',  '', 3, 1, 1),
(300,  0, '实训计划管理',     1, '/training',   NULL, 'Briefcase',     '', 4, 1, 1),
(400,  0, '资源管理',     1, '/resource',   NULL,                    'folder',   '', 5, 1, 1),
(500,  0, '系统管理',     1, '/system',     NULL,                    'setting',  '', 9, 1, 1),
(600,  0, '系统监控',     1, '/monitor',    NULL,                    'monitor',  '', 10, 1, 1),
-- 课程计划管理
(101, 100, '课程管理',     2, '/course/manage',     'course/CourseList',    NULL, 'course:list', 1, 1, 1),
(102, 100, '章节与内容',   2, '/course/chapter',    'course/ChapterList',   NULL, 'course:query', 2, 1, 1),
(103, 100, '教学计划',     2, '/course/plan',       'course/PlanList',      NULL, 'course:plan:list', 3, 1, 1),
(110, 101, '新增', 3, NULL, NULL, NULL, 'course:add', 1, 1, 1),
(111, 101, '编辑', 3, NULL, NULL, NULL, 'course:edit', 2, 1, 1),
(112, 101, '删除', 3, NULL, NULL, NULL, 'course:remove', 3, 1, 1),
(120, 103, '新增计划', 3, NULL, NULL, NULL, 'course:plan:add', 1, 1, 1),
(121, 103, '编辑计划', 3, NULL, NULL, NULL, 'course:plan:edit', 2, 1, 1),
(122, 103, '删除计划', 3, NULL, NULL, NULL, 'course:plan:remove', 3, 1, 1),
(123, 103, '审核计划', 3, NULL, NULL, NULL, 'course:plan:approve', 4, 1, 1),
(124, 103, '查看详情', 3, NULL, NULL, NULL, 'course:plan:query', 5, 1, 1),
-- 授课管理（课程-教师多对多关系；与 /course/teacher/* 接口配套）
(104, 100, '授课管理',  2, '/course/teach', 'course/TeachList', NULL, 'teach:list',  4, 1, 1),
(140, 104, '查询',     3, NULL, NULL, NULL, 'teach:query',    1, 1, 1),
(141, 104, '新增',     3, NULL, NULL, NULL, 'teach:add',      2, 1, 1),
(142, 104, '编辑',     3, NULL, NULL, NULL, 'teach:edit',     3, 1, 1),
(143, 104, '删除',     3, NULL, NULL, NULL, 'teach:remove',   4, 1, 1),
(144, 104, '分配',     3, NULL, NULL, NULL, 'teach:assign',   5, 1, 1),
-- 课程实验计划管理
(201, 200, '实验计划', 2, '/experiment/plan', 'experiment/PlanList', NULL, 'experiment:plan:list', 1, 1, 1),
(210, 201, '新增', 3, NULL, NULL, NULL, 'experiment:plan:add', 1, 1, 1),
(211, 201, '编辑', 3, NULL, NULL, NULL, 'experiment:plan:edit', 2, 1, 1),
(212, 201, '删除', 3, NULL, NULL, NULL, 'experiment:plan:remove', 3, 1, 1),
(213, 201, '审核', 3, NULL, NULL, NULL, 'experiment:plan:approve', 4, 1, 1),
(214, 201, '查看详情', 3, NULL, NULL, NULL, 'experiment:plan:query', 5, 1, 1),
-- 实训计划管理
(301, 300, '实训计划', 2, '/training/plan', 'training/PlanList', NULL, 'training:plan:list', 1, 1, 1),
(302, 300, '报名管理', 2, '/training/registration', 'training/RegistrationList', NULL, 'training:registration:list', 2, 1, 1),
(310, 301, '新增', 3, NULL, NULL, NULL, 'training:plan:add', 1, 1, 1),
(311, 301, '编辑', 3, NULL, NULL, NULL, 'training:plan:edit', 2, 1, 1),
(312, 301, '删除', 3, NULL, NULL, NULL, 'training:plan:remove', 3, 1, 1),
(313, 301, '审核', 3, NULL, NULL, NULL, 'training:plan:approve', 4, 1, 1),
(320, 302, '新增报名', 3, NULL, NULL, NULL, 'training:registration:add', 1, 1, 1),
(321, 302, '审核报名', 3, NULL, NULL, NULL, 'training:registration:review', 2, 1, 1),
(322, 302, '登记成绩', 3, NULL, NULL, NULL, 'training:registration:grade', 3, 1, 1),
(323, 302, '删除报名', 3, NULL, NULL, NULL, 'training:registration:remove', 4, 1, 1),
-- 资源管理
(401, 400, '资源分类', 2, '/resource/category', 'resource/CategoryList', NULL, 'resource:category:list', 1, 1, 1),
(402, 400, '资源列表', 2, '/resource/list',     'resource/ResourceList', NULL, 'resource:list', 2, 1, 1),
(410, 401, '新增', 3, NULL, NULL, NULL, 'resource:category:add', 1, 1, 1),
(411, 401, '编辑', 3, NULL, NULL, NULL, 'resource:category:edit', 2, 1, 1),
(412, 401, '删除', 3, NULL, NULL, NULL, 'resource:category:remove', 3, 1, 1),
(420, 402, '新增', 3, NULL, NULL, NULL, 'resource:add', 1, 1, 1),
(421, 402, '编辑', 3, NULL, NULL, NULL, 'resource:edit', 2, 1, 1),
(422, 402, '删除', 3, NULL, NULL, NULL, 'resource:remove', 3, 1, 1),
-- 网站门户
(10,   1, '轮播图', 2, '/portal/banner',    'portal/BannerList',    NULL, 'portal:article:list', 1, 1, 1),
(11,   1, '通知公告', 2, '/portal/notice',   'portal/NoticeList',    NULL, 'portal:article:list', 2, 1, 1),
(12,   1, '新闻资讯', 2, '/portal/news',     'portal/NewsList',      NULL, 'portal:article:list', 3, 1, 1),
(20,  10, '新增', 3, NULL, NULL, NULL, 'portal:article:add', 1, 1, 1),
(21,  10, '编辑', 3, NULL, NULL, NULL, 'portal:article:edit', 2, 1, 1),
(22,  10, '删除', 3, NULL, NULL, NULL, 'portal:article:remove', 3, 1, 1),
-- 系统管理
(501, 500, '用户管理', 2, '/system/user',    'system/UserList',    NULL, 'system:user:list', 1, 1, 1),
(502, 500, '部门管理', 2, '/system/dept',    'system/DeptList',    NULL, 'system:dept:list', 2, 1, 1),
(503, 500, '角色管理', 2, '/system/role',    'system/RoleList',    NULL, 'system:role:list', 3, 1, 1),
(504, 500, '菜单管理', 2, '/system/menu',    'system/MenuList',    NULL, 'system:menu:list', 4, 1, 1),
(505, 500, '字典管理', 2, '/system/dict',    'system/DictList',    NULL, 'system:dict:type:list', 5, 1, 1),
(506, 500, '参数设置', 2, '/system/config',  'system/ConfigList',  NULL, 'system:config:list', 6, 1, 1),
(510, 501, '新增', 3, NULL, NULL, NULL, 'system:user:add', 1, 1, 1),
(511, 501, '编辑', 3, NULL, NULL, NULL, 'system:user:edit', 2, 1, 1),
(512, 501, '删除', 3, NULL, NULL, NULL, 'system:user:remove', 3, 1, 1),
(513, 501, '重置密码', 3, NULL, NULL, NULL, 'system:user:resetPwd', 4, 1, 1),
(520, 503, '新增', 3, NULL, NULL, NULL, 'system:role:add', 1, 1, 1),
(521, 503, '编辑', 3, NULL, NULL, NULL, 'system:role:edit', 2, 1, 1),
(522, 503, '删除', 3, NULL, NULL, NULL, 'system:role:remove', 3, 1, 1),
(530, 504, '新增', 3, NULL, NULL, NULL, 'system:menu:add', 1, 1, 1),
(531, 504, '编辑', 3, NULL, NULL, NULL, 'system:menu:edit', 2, 1, 1),
(532, 504, '删除', 3, NULL, NULL, NULL, 'system:menu:remove', 3, 1, 1),
-- 系统监控
(601, 600, '服务器监控', 2, '/monitor/server',         'monitor/Server',  NULL, 'monitor:server:list', 1, 1, 1),
(602, 600, '登录日志',   2, '/monitor/login-log',      'monitor/LoginLog', NULL, 'monitor:loginLog:list', 2, 1, 1),
(603, 600, '操作日志',   2, '/monitor/operation-log',  'monitor/OperationLog', NULL, 'monitor:operationLog:list', 3, 1, 1),
-- 补充按钮：用于对齐 Controller 上 @PreAuthorize 的全部权限标识
-- 系统管理
(700, 501, '查询',     3, NULL, NULL, NULL, 'system:user:query',        5, 1, 1),
(701, 502, '新增',     3, NULL, NULL, NULL, 'system:dept:add',         2, 1, 1),
(702, 502, '编辑',     3, NULL, NULL, NULL, 'system:dept:edit',        3, 1, 1),
(703, 502, '删除',     3, NULL, NULL, NULL, 'system:dept:remove',      4, 1, 1),
(704, 503, '查询',     3, NULL, NULL, NULL, 'system:role:query',       5, 1, 1),
(710, 505, '类型新增', 3, NULL, NULL, NULL, 'system:dict:type:add',    2, 1, 1),
(711, 505, '类型编辑', 3, NULL, NULL, NULL, 'system:dict:type:edit',   3, 1, 1),
(712, 505, '类型删除', 3, NULL, NULL, NULL, 'system:dict:type:remove', 4, 1, 1),
(713, 505, '数据列表', 3, NULL, NULL, NULL, 'system:dict:data:list',   5, 1, 1),
(714, 505, '数据新增', 3, NULL, NULL, NULL, 'system:dict:data:add',    6, 1, 1),
(715, 505, '数据编辑', 3, NULL, NULL, NULL, 'system:dict:data:edit',   7, 1, 1),
(716, 505, '数据删除', 3, NULL, NULL, NULL, 'system:dict:data:remove', 8, 1, 1),
(720, 506, '新增',     3, NULL, NULL, NULL, 'system:config:add',       2, 1, 1),
(721, 506, '编辑',     3, NULL, NULL, NULL, 'system:config:edit',      3, 1, 1),
(722, 506, '删除',     3, NULL, NULL, NULL, 'system:config:remove',    4, 1, 1),
-- 业务模块
(730, 201, '查看详情', 3, NULL, NULL, NULL, 'experiment:plan:query',         5, 1, 1),
(731, 301, '查看详情', 3, NULL, NULL, NULL, 'training:plan:query',           5, 1, 1),
(732, 302, '新增报名', 3, NULL, NULL, NULL, 'training:registration:add',     3, 1, 1),
(733, 302, '删除报名', 3, NULL, NULL, NULL, 'training:registration:remove',  4, 1, 1),
-- 系统监控
(739, 602, '删除',     3, NULL, NULL, NULL, 'monitor:loginLog:remove',      1, 1, 1),
(740, 603, '删除',     3, NULL, NULL, NULL, 'monitor:operationLog:remove',   2, 1, 1),
-- 8. 作业管理（顶级目录 750；教师子菜单 751/752 + 按钮；学生子菜单 753/754 + 按钮）
(750, 0,   '作业管理',     1, '/assignment',                NULL,                       'Notebook', '',  6, 1, 1),
-- 教师侧
(751, 750, '作业列表',     2, '/assignment/list',                 'course/AssignmentList',  NULL, 'assignment:list',             1, 1, 1),
(752, 750, '提交批改',     2, '/assignment/submission-list',     'course/SubmissionList',  NULL, 'assignment:submission:list',  2, 1, 1),
(760, 751, '新增作业',     3, NULL, NULL, NULL, 'assignment:add',     1, 1, 1),
(761, 751, '编辑作业',     3, NULL, NULL, NULL, 'assignment:edit',     2, 1, 1),
(762, 751, '删除作业',     3, NULL, NULL, NULL, 'assignment:remove',   3, 1, 1),
(763, 751, '发布作业',     3, NULL, NULL, NULL, 'assignment:publish',  4, 1, 1),
(764, 751, '关闭作业',     3, NULL, NULL, NULL, 'assignment:close',    5, 1, 1),
(765, 751, '查看详情',     3, NULL, NULL, NULL, 'assignment:query',    6, 1, 1),
(770, 752, '批改评分',     3, NULL, NULL, NULL, 'assignment:grade',    1, 1, 1),
-- 学生侧
(753, 750, '我的作业',     2, '/student/assignment/list',        'student/MyAssignmentList',  NULL, 'assignment:my:list',   3, 1, 1),
(780, 753, '查看',         3, NULL, NULL, NULL, 'assignment:my:query', 1, 1, 1),
(781, 753, '保存草稿',     3, NULL, NULL, NULL, 'assignment:save',     2, 1, 1),
(782, 753, '提交作业',     3, NULL, NULL, NULL, 'assignment:submit',   3, 1, 1),
(783, 753, '删除草稿',     3, NULL, NULL, NULL, 'assignment:my:remove', 4, 1, 1),
-- 9. 班级管理（系统管理下的子菜单 507；按钮 540-544）
(507, 500, '班级管理',  2, '/system/class', 'system/ClassList', 'User', 'class:list',          7, 1, 1),
(540, 507, '新增',      3, NULL, NULL, NULL, 'class:add',             1, 1, 1),
(541, 507, '编辑',      3, NULL, NULL, NULL, 'class:edit',            2, 1, 1),
(542, 507, '删除',      3, NULL, NULL, NULL, 'class:remove',          3, 1, 1),
(543, 507, '查询',      3, NULL, NULL, NULL, 'class:query',           4, 1, 1),
(544, 507, '分配成员',  3, NULL, NULL, NULL, 'class:member:assign',   5, 1, 1);

-- 超级管理员(role=1)分配所有菜单
INSERT INTO `sys_role_menu`(`id`, `role_id`, `menu_id`)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 1000, 1, id FROM sys_menu;

-- ---------------------------------------------------------------------
-- 系统内置默认角色权限分配
-- id 段分配：5000+ ROLE_SYSTEM / 6000+ ROLE_TEACHER / 7000+ ROLE_STUDENT
-- ---------------------------------------------------------------------

-- 系统管理员(role=2)：资源管理 + 系统管理 + 系统监控（不含业务模块、门户内容）
INSERT INTO `sys_role_menu`(`id`, `role_id`, `menu_id`)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 5000, 2, id FROM sys_menu
WHERE id IN (
    -- 资源管理
    400, 401, 402, 410, 411, 412, 420, 421, 422,
    -- 系统管理（含班级）
    500, 501, 502, 503, 504, 505, 506, 507,
    510, 511, 512, 513,
    520, 521, 522,
    530, 531, 532,
    540, 541, 542, 543, 544,
    700, 701, 702, 703, 704,
    710, 711, 712, 713, 714, 715, 716,
    720, 721, 722,
    -- 系统监控
    600, 601, 602, 603, 739, 740
)
AND id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);

-- 教师(role=3)：课程计划管理 + 课程实验计划管理 + 实训计划管理 + 作业管理（教师侧）
INSERT INTO `sys_role_menu`(`id`, `role_id`, `menu_id`)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 6000, 3, id FROM sys_menu
WHERE id IN (
    -- 课程计划管理
    100, 101, 102, 103,
    110, 111, 112,
    120, 121, 122, 123, 124,
    -- 课程实验计划管理
    200, 201,
    210, 211, 212, 213, 214, 730,
    -- 实训计划管理
    300, 301, 302,
    310, 311, 312, 313,
    320, 321, 322, 323,
    731, 732, 733,
    -- 作业管理（教师子菜单：751/752 + 教师按钮；不含学生子菜单 753）
    750, 751, 752,
    760, 761, 762, 763, 764, 765,
    770
)
AND id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 3);

-- 学生(role=4)：课程计划（只读+详情）+ 实验计划（只读+详情）+ 实训（看+报名）+ 我的作业
INSERT INTO `sys_role_menu`(`id`, `role_id`, `menu_id`)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 7000, 4, id FROM sys_menu
WHERE id IN (
    -- 课程计划管理（只读，不含新增/编辑/删除/审核；含详情查询）
    100, 101, 102, 103, 124,
    -- 课程实验计划管理（只读 + 详情）
    200, 201, 214, 730,
    -- 实训计划管理（看 + 报名/取消报名；不含审核/登记成绩）
    300, 301, 302,
    731, 732, 733,
    -- 作业管理（仅学生子菜单 753 + 学生按钮）
    750, 753,
    780, 781, 782, 783
)
AND id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 4);

-- 字典类型
INSERT INTO `sys_dict_type`(`id`, `dict_name`, `dict_type`, `description`, `status`) VALUES
(1, '课程性质', 'course_type', '课程性质字典', 1),
(2, '资源类型', 'resource_type', '教学资源类型', 1),
(3, '课程内容类型', 'content_type', '课程内容类型', 1),
(4, '门户内容类型', 'portal_type', '门户内容类型', 1),
(5, '学期', 'semester', '当前学期列表', 1);

INSERT INTO `sys_dict_data`(`id`, `dict_type`, `dict_label`, `dict_value`, `list_class`, `sort`, `status`, `is_default`) VALUES
-- 课程性质（仅保留必修、选修）
(1, 'course_type', '必修', '1', 'primary', 1, 1, 1),
(2, 'course_type', '选修', '2', 'success', 2, 1, 0),
-- 资源类型
(20, 'resource_type', '文档', '1', 'primary', 1, 1, 0),
(21, 'resource_type', '图片', '2', 'info',    2, 1, 0),
(22, 'resource_type', '视频', '3', 'danger',  3, 1, 0),
(23, 'resource_type', '音频', '4', 'warning', 4, 1, 0),
(24, 'resource_type', '压缩包', '5', 'success', 5, 1, 0),
(25, 'resource_type', '其他', '6', 'default', 6, 1, 0),
-- 内容类型
(30, 'content_type', '课件PPT', '1', 'primary', 1, 1, 0),
(31, 'content_type', '视频',   '2', 'danger',  2, 1, 0),
(32, 'content_type', '文档',   '3', 'info',    3, 1, 0),
(34, 'content_type', '富文本', '5', 'warning', 4, 1, 0),
-- 门户内容类型
(40, 'portal_type', '轮播图',   '1', 'primary', 1, 1, 0),
(41, 'portal_type', '通知公告', '2', 'warning', 2, 1, 0),
(42, 'portal_type', '新闻资讯', '3', 'success', 3, 1, 0),
-- 学期
(50, 'semester', '2024-2025-1', '2024-2025-1', 'primary', 1, 1, 0),
(51, 'semester', '2024-2025-2', '2024-2025-2', 'primary', 2, 1, 0),
(52, 'semester', '2025-2026-1', '2025-2026-1', 'primary', 3, 1, 1),
(53, 'semester', '2025-2026-2', '2025-2026-2', 'primary', 4, 1, 0),
(54, 'semester', '2026-2027-1', '2026-2027-1', 'primary', 5, 1, 0),
(55, 'semester', '2026-2027-2', '2026-2027-2', 'primary', 6, 1, 0),
(56, 'semester', '2027-2028-1', '2027-2028-1', 'primary', 7, 1, 0),
(57, 'semester', '2027-2028-2', '2027-2028-2', 'primary', 8, 1, 0),
(58, 'semester', '2028-2029-1', '2028-2029-1', 'primary', 9, 1, 0),
(59, 'semester', '2028-2029-2', '2028-2029-2', 'primary', 10, 1, 0),
(60, 'semester', '2029-2030-1', '2029-2030-1', 'primary', 11, 1, 0),
(61, 'semester', '2029-2030-2', '2029-2030-2', 'primary', 12, 1, 0);

-- 系统参数
INSERT INTO `sys_config`(`id`, `config_name`, `config_key`, `config_value`, `config_type`, `remark`) VALUES
(1, '系统名称',         'sys.title',              '智能化在线教学支持服务平台',  1, '站内显示的系统名称'),
(2, '版权信息',         'sys.copyright',          'Copyright © 2024 SmartTeach', 1, '页脚版权'),
(3, '门户首页轮播图数', 'portal.banner.limit',    '5',                          0, '门户首页轮播图展示数量'),
(4, '上传文件最大MB',   'file.upload.max-mb',     '100',                        0, '单次上传最大文件大小（MB）'),
(5, '用户默认密码',     'sys.user.init-password', '123456',                     0, '新增用户时的初始密码'),
(6, '备案号',           'sys.icp',                '',                            0, '公网备案号');

-- 部门
INSERT INTO `sys_dept`(`id`, `parent_id`, `dept_name`, `dept_code`, `sort`, `leader`, `status`) VALUES
(1, 0, '智能与信息工程学院', 'SCHOOL',     1, '院长', 1),
(2, 1, '计算机科学系',     'DEPT_CS',    1, '系主任', 1),
(3, 1, '软件工程系',       'DEPT_SE',    2, '系主任', 1),
(4, 1, '网络空间安全系',   'DEPT_CYBER', 3, '系主任', 1),
(5, 1, '人工智能系',       'DEPT_AI',    4, '系主任', 1);

-- 资源分类
INSERT INTO `resource_category`(`id`, `parent_id`, `category_name`, `category_code`, `sort`, `status`) VALUES
(1, 0, '课程资源',  'COURSE',     1, 1),
(2, 0, '实验资源',  'EXPERIMENT', 2, 1),
(3, 0, '实训资源',  'TRAINING',   3, 1),
(4, 0, '教学素材',  'MATERIAL',   4, 1),
(11, 1, '教学课件',  'PPT',    1, 1),
(12, 1, '教学视频',  'VIDEO',  2, 1),
(13, 1, '教学大纲',  'OUTLINE', 3, 1);

-- 门户示例数据
INSERT INTO `portal_article`(`id`, `type`, `title`, `cover_image`, `content`, `sort`, `top`, `status`, `publish_time`) VALUES
(1, 1, '欢迎使用智能教学平台', '', '<p>欢迎访问智能教学平台</p>', 1, 1, 1, NOW()),
(2, 2, '关于2024秋季学期教学安排的通知', '', '<p>各位老师、同学：2024秋季学期教学安排详见附件。</p>', 1, 1, 1, NOW()),
(3, 3, '我院成功举办第一届教学改革研讨会', '', '<p>近日，我院成功举办第一届教学改革研讨会...</p>', 1, 0, 1, NOW()),
(4, 3, '校企合作签约仪式顺利举行', '', '<p>近日，我院与多家知名企业签署校企合作协议。</p>', 2, 0, 1, NOW());

-- 课程示例数据（让实验/教学/实训计划能选到课程）
INSERT INTO `course`(`id`, `course_code`, `course_name`, `category_id`, `category_name`, `description`, `teacher_id`, `teacher_name`, `credit`, `total_hours`, `course_type`, `status`) VALUES
(1, 'CS101', '计算机科学导论',         1, '课程资源', '本课程介绍计算机科学的基本概念、发展历史与应用领域。', 1, '超级管理员', 3.0, 48, 1, 1),
(2, 'CS201', '数据结构',               1, '课程资源', '线性表、树、图等基本数据结构及其算法实现。',           1, '超级管理员', 4.0, 64, 1, 1),
(3, 'CS301', '操作系统',               1, '课程资源', '进程管理、内存管理、文件系统、设备管理等内容。',       1, '超级管理员', 4.0, 64, 1, 1),
(4, 'CS302', '数据库系统原理',         1, '课程资源', '关系数据库理论、SQL语言、事务处理与并发控制。',         1, '超级管理员', 3.5, 56, 1, 1),
(5, 'SE201', '软件工程',               2, '课程资源', '软件生命周期、需求分析、设计、测试与维护。',           1, '超级管理员', 3.5, 56, 1, 1),
(6, 'SE301', 'Web应用开发',           2, '课程资源', '前后端分离架构、Vue + Spring Boot 全栈开发实战。',     1, '超级管理员', 3.0, 48, 2, 1),
(7, 'AI301', '人工智能导论',           4, '课程资源', '搜索、知识表示、机器学习与深度学习基础。',             1, '超级管理员', 3.0, 48, 1, 1),
(8, 'CYB201','网络安全基础',          3, '课程资源', '密码学、网络协议安全、常见攻防技术。',                 1, '超级管理员', 3.0, 48, 2, 1);

-- 课程-教师授课关系回填：8 门 seed 课每条都分配给当前 course.teacher_id（=1 超级管理员）
INSERT INTO `course_teacher`(`id`, `course_id`, `teacher_id`, `role`, `status`) VALUES
(1, 1, 1, '主讲', 1),
(2, 2, 1, '主讲', 1),
(3, 3, 1, '主讲', 1),
(4, 4, 1, '主讲', 1),
(5, 5, 1, '主讲', 1),
(6, 6, 1, '主讲', 1),
(7, 7, 1, '主讲', 1),
(8, 8, 1, '主讲', 1);

USE `smart_teach_platform`;

-- =====================================================================
-- 补充测试数据脚本 - 智能化在线教学支持服务平台
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 用户扩展数据 (教师与学生账户)
-- 密码均为 123456 (对应的 BCrypt 哈希值如下)
-- ---------------------------------------------------------------------
INSERT INTO `sys_user`(`id`, `username`, `password`, `real_name`, `phone`, `email`, `gender`, `dept_id`, `status`, `remark`) VALUES 
(1001, 'teacher1', '$2a$10$GA1/Xml8Iryf5TphFd0la.DU.1xLSjBMyReH31Z.qmTPVL84GC/h2', '张教授', '13800138001', 'zhang@smartteach.edu.cn', 1, 2, 1, '计算机科学系专业课教师'),
(1002, 'teacher2', '$2a$10$GA1/Xml8Iryf5TphFd0la.DU.1xLSjBMyReH31Z.qmTPVL84GC/h2', '李副教授', '13800138002', 'li@smartteach.edu.cn', 2, 3, 1, '软件工程系骨干教师'),
(2001, 'student1', '$2a$10$GA1/Xml8Iryf5TphFd0la.DU.1xLSjBMyReH31Z.qmTPVL84GC/h2', '王小明', '18611112222', 'wangxm@std.edu.cn', 1, 2, 1, '计科2201班学生'),
(2002, 'student2', '$2a$10$GA1/Xml8Iryf5TphFd0la.DU.1xLSjBMyReH31Z.qmTPVL84GC/h2', '赵美美', '18611113333', 'zhaomm@std.edu.cn', 2, 3, 1, '软工2202班学生');

-- 分配角色
INSERT INTO `sys_user_role`(`id`, `user_id`, `role_id`) VALUES
(101, 1001, 3), -- 张教授 -> 教师
(102, 1002, 3), -- 李副教授 -> 教师
(201, 2001, 4), -- 王小明 -> 学生
(202, 2002, 4); -- 赵美美 -> 学生

-- 班级（隶属现有部门：计算机科学系 id=2、软件工程系 id=3）
INSERT INTO `sys_class`(`id`, `class_name`, `grade`, `dept_id`, `sort`, `status`) VALUES
(1, '计科2201班', '2022', 2, 1, 1),
(2, '软工2202班', '2022', 3, 2, 1);

-- 用户-班级关系（学生归属本班；教师按"任课"维度挂到对应班）
INSERT INTO `sys_user_class`(`id`, `user_id`, `class_id`) VALUES
(1, 2001, 1), -- 王小明 -> 计科2201班
(2, 2002, 2), -- 赵美美 -> 软工2202班
(3, 1001, 1), -- 张教授 -> 计科2201班（任课教师）
(4, 1001, 2); -- 张教授 -> 软工2202班（跨班任课）


-- ---------------------------------------------------------------------
-- 2. 课程章节与内容 (以数据结构、Web应用开发课程为例)
-- ---------------------------------------------------------------------
-- 课程章节 (course_chapter)
INSERT INTO `course_chapter`(`id`, `course_id`, `parent_id`, `chapter_title`, `sort`, `hours`) VALUES 
(1100, 2, 0, '第一章 绪论', 1, 4),
(1101, 2, 1100, '1.1 数据结构的基本概念', 1, 2),
(1102, 2, 1100, '1.2 算法与算法分析', 2, 2),
(1200, 2, 0, '第二章 线性表', 2, 8),
(1201, 2, 1200, '2.1 线性表的顺序表示', 1, 4),
(1202, 2, 1200, '2.2 线性表的链式表示', 2, 4),
(1300, 6, 0, '第一章 Vue.js 基础进阶', 1, 6);

-- 课程内容 (course_content)
INSERT INTO `course_content`(`id`, `course_id`, `chapter_id`, `content_title`, `content_type`, `resource_id`, `resource_url`, `sort`, `hours`, `status`) VALUES 
(5001, 2, 1101, '绪论核心概念课件', 1, 4001, '/files/ppt/ds_chap1.ppt', 1, 1, 1),
(5002, 2, 1102, '算法复杂度分析教学视频', 2, 4002, '/files/video/algorithm_complexity.mp4', 1, 1, 1),
(5003, 2, 1201, '单链表经典面试题集锦', 4, NULL, 'https://leetcode.cn/tag/linked-list/', 1, 0, 1),
(5004, 2, 1202, '双向链表的实现原理', 5, NULL, NULL, 1, 2, 1);

-- 补充富文本内容
UPDATE `course_content` SET `rich_text` = '<h3>双向链表核心逻辑</h3><p>双向链表的每个节点有两个指针域，一个指向直接前驱，一个指向直接后继。</p>' WHERE `id` = 5004;


-- ---------------------------------------------------------------------
-- 3. 课程教学计划
-- ---------------------------------------------------------------------
-- 教学计划主表 (course_plan)
INSERT INTO `course_plan`(`id`, `plan_title`, `course_id`, `course_name`, `semester`, `class_name`, `start_date`, `end_date`, `total_weeks`, `description`, `status`, `approver_id`, `approver_name`) VALUES 
(10001, '2025秋学期《数据结构》授课计划', 2, '数据结构', '2025-2026-1', '计科2201班', '2025-09-01', '2026-01-10', 18, '针对计算机科学与技术专业大二学生的教学计划。', 1, 1, '超级管理员'),
(10002, '2025秋学期《Web应用开发》教学大纲计划', 6, 'Web应用开发', '2025-2026-1', '软工2202班', '2025-09-01', '2026-01-10', 16, '前后端分离架构实战课程。', 0, NULL, NULL); -- 草稿状态

-- 教学计划明细 (course_plan_item)
INSERT INTO `course_plan_item`(`id`, `plan_id`, `week_no`, `chapter_id`, `chapter_title`, `content`, `objective`, `method`, `hours`) VALUES 
(10011, 10001, 1, 1100, '第一章 绪论', '讲解数据结构概念、时间复杂度度量基础。', '掌握抽象数据类型定义，学会推导大O渐进复杂度。', '多媒体讲授 + 课堂互动白板', 4),
(10012, 10001, 2, 1201, '2.1 线性表的顺序表示', '顺序表（数组）的插入、删除及内存连续性特点。', '熟练掌握动态数组扩容机制及边界指针条件。', '板书推演 + 上机现场编码', 4);


-- ---------------------------------------------------------------------
-- 4. 课程实验计划
-- ---------------------------------------------------------------------
-- 实验计划主表 (experiment_plan)
INSERT INTO `experiment_plan`(`id`, `plan_title`, `course_id`, `course_name`, `semester`, `class_name`, `teacher_id`, `teacher_name`, `lab_room`, `start_date`, `end_date`, `total_experiments`, `total_hours`, `status`) VALUES 
(20001, '《数据结构》配套实验上机计划', 2, '数据结构', '2025-2026-1', '计科2201班', 1001, '张教授', '科教楼402机房', '2025-09-10', '2025-12-25', 4, 16, 1);

-- 实验计划明细 (experiment_plan_item)
INSERT INTO `experiment_plan_item`(`id`, `plan_id`, `exp_no`, `exp_name`, `exp_type`, `purpose`, `content`, `requirement`, `class_date`, `class_period`, `hours`, `teacher_name`) VALUES 
(20011, 20001, 1, '约瑟夫环问题的链表实现', 2, '掌握循环链表的创建、遍历和节点删除。', '用循环单链表模拟约瑟夫出列问题，输出出列顺序。', '独立编写C/C++代码，处理好头尾节点衔接与内存释放。', '2025-09-20', '周四 3-4节', 4, '张教授'),
(20012, 20001, 2, '二叉树的建立与遍历算法', 3, '深入理解二叉树的递归与非递归周游。', '实现二叉树的前序、中序、后序遍历及层次遍历。', '需支持从前序+中序序列逆向重构二叉树。', '2025-10-15', '周四 3-4节', 4, '张教授');


-- ---------------------------------------------------------------------
-- 5. 实训计划与实训报名
-- ---------------------------------------------------------------------
-- 实训计划 (training_plan)
INSERT INTO `training_plan`(`id`, `plan_title`, `project_name`, `course_id`, `course_name`, `semester`, `class_name`, `teacher_id`, `teacher_name`, `location`, `start_date`, `end_date`, `duration_days`, `total_hours`, `capacity`, `objective`, `status`) VALUES 
(30001, '企业级企业中台级全栈开发企业实训', '基于Spring Cloud的在线协同办公中台系统开发', 6, 'Web应用开发', '2025-2026-1', '软工大三混合班', 1002, '李副教授', '实训楼A栋301软件工程创新实验室', '2026-01-12', '2026-01-26', 14, 80, 50, '培养学生大型分布式微服务项目的协同开发与DevOps工程实践能力。', 1);

-- 实训报名明细 (training_registration)
INSERT INTO `training_registration`(`id`, `plan_id`, `plan_title`, `student_id`, `student_name`, `class_name`, `phone`, `status`, `score`, `comment`) VALUES 
(3011, 30001, '企业级企业中台级全栈开发企业实训', 2001, '王小明', '计科2201班', '18611112222', 1, 92.50, '项目架构设计清晰，答辩表现优异'), -- 已通过且已给成绩
(3012, 30001, '企业级企业中台级全栈开发企业实训', 2002, '赵美美', '软工2202班', '18611113333', 0, NULL, NULL); -- 待审核状态

-- ---------------------------------------------------------------------
-- 7. 系统日志 (模拟真实系统的访问活动)
-- ---------------------------------------------------------------------
-- 登录日志 (sys_login_log)
INSERT INTO `sys_login_log`(`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES 
(9001, 'admin', '192.168.1.10', '局域网/校内网', 'Chrome 120.0', 'Windows 11', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(9002, 'teacher1', '10.22.45.18', '办公楼无线网', 'Edge 120.0', 'macOS Sonoma', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(9003, 'student1', '172.16.102.5', '学生宿舍网络', 'Chrome Mobile', 'Android 14', 0, '验证码错误', NOW());

-- 操作日志 (sys_operation_log)
INSERT INTO `sys_operation_log`(`id`, `module`, `action`, `method`, `request_uri`, `http_method`, `params`, `result`, `ip`, `user_id`, `username`, `status`, `cost_time`, `operation_time`) VALUES 
(9501, '课程管理', '修改课程发布状态', 'com.smartteach.controller.CourseController.updateStatus()', '/api/course/status', 'PUT', '{"id":2, "status":1}', '{"code":200, "msg":"success"}', '10.22.45.18', 1001, 'teacher1', 1, 45, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(9502, '教学计划', '新增计划明细', 'com.smartteach.controller.CoursePlanController.addItem()', '/api/course/plan/item', 'POST', '{"planId":10001, "weekNo":1, "chapterTitle":"绪论"}', '{"code":200, "msg":"success"}', '10.22.45.18', 1001, 'teacher1', 1, 112, DATE_SUB(NOW(), INTERVAL 45 MINUTE));

-- =====================================================================
-- 8. 前台门户扩展：代码库(codex_snippet) + 站内消息(message_notify)
-- =====================================================================

-- 代码库（CodeX）—— 公开访问的代码片段表
DROP TABLE IF EXISTS `codex_snippet`;
CREATE TABLE `codex_snippet` (
    `id`          BIGINT        NOT NULL,
    `title`       VARCHAR(200)  NOT NULL                COMMENT '片段标题',
    `language`    VARCHAR(20)   NOT NULL DEFAULT 'text'  COMMENT '语言（java/python/js/...）',
    `code`        MEDIUMTEXT                            COMMENT '完整代码',
    `preview`     VARCHAR(500)                          COMMENT '列表中展示的预览行',
    `description` VARCHAR(500)                          COMMENT '说明',
    `tags`        VARCHAR(255)                          COMMENT '逗号分隔的标签',
    `author`      VARCHAR(50)                           COMMENT '作者',
    `views`       INT           NOT NULL DEFAULT 0      COMMENT '浏览次数',
    `is_public`   TINYINT       NOT NULL DEFAULT 1      COMMENT '是否公开：0仅自己可见 1公开',
    `create_by`   BIGINT                                DEFAULT NULL,
    `update_by`   BIGINT                                DEFAULT NULL,
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_language` (`language`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB COMMENT ='前台代码片段库';

INSERT INTO `codex_snippet`(`id`, `title`, `language`, `code`, `preview`, `description`, `tags`, `author`, `views`, `is_public`) VALUES
(1, 'Java 通用分页响应封装', 'java', 'public class PageResult<T> {\n    private long total;\n    private long pageNum;\n    private long pageSize;\n    private List<T> list;\n}', 'public class PageResult<T> {\n    private long total;\n    private long pageNum;\n    private long pageSize;\n    private List<T> list;\n}', 'MyBatis-Plus 分页响应统一封装', 'java,mybatis-plus,page', 'Methōdus', 86, 1),
(2, 'Python 二分查找模板', 'python', 'def bsearch(nums, target):\n    lo, hi = 0, len(nums) - 1\n    while lo <= hi:\n        mid = (lo + hi) // 2\n        if nums[mid] == target:\n            return mid\n        elif nums[mid] < target:\n            lo = mid + 1\n        else:\n            hi = mid - 1\n    return -1', 'def bsearch(nums, target):\n    lo, hi = 0, len(nums) - 1\n    while lo <= hi:', '标准二分查找闭区间写法', 'python,algorithm,binary-search', 'Methōdus', 132, 1),
(3, 'JS 防抖与节流', 'js', 'export const debounce = (fn, wait = 300) => {\n  let timer = null;\n  return function (...args) {\n    if (timer) clearTimeout(timer);\n    timer = setTimeout(() => fn.apply(this, args), wait);\n  };\n};', 'export const debounce = (fn, wait = 300) => {\n  let timer = null;', 'Web 开发高频工具函数', 'js,frontend,utils', 'Methōdus', 204, 1),
(4, 'SpringBoot 统一返回体', 'java', '@Data\npublic class Result<T> {\n    private int code;\n    private String message;\n    private T data;\n    public static <T> Result<T> success(T data) { return new Result<>(200, "OK", data); }\n}', '@Data\npublic class Result<T> {\n    private int code;\n    private String message;\n    private T data;', '后端 Result 响应封装样例', 'java,springboot,result', 'Methōdus', 95, 1),
(5, 'MySQL 慢查询排查 SQL', 'sql', '-- 查看最近 100 条慢 SQL\nSELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 100;\n-- 按出现频次聚合\nSELECT digest_text, COUNT(*) cnt FROM performance_schema.events_statements_summary_by_digest\nWHERE schema_name = DATABASE() GROUP BY digest_text ORDER BY cnt DESC LIMIT 20;', '-- 查看最近 100 条慢 SQL\nSELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 100;', 'DBA 常用的慢查询定位 SQL', 'sql,mysql,dba', 'Methōdus', 41, 1);

-- 站内消息中心
DROP TABLE IF EXISTS `message_notify`;
CREATE TABLE `message_notify` (
    `id`          BIGINT       NOT NULL,
    `user_id`     BIGINT       NOT NULL                COMMENT '接收人用户ID',
    `type`        VARCHAR(20)  NOT NULL DEFAULT 'system' COMMENT '类型：system/system-audit/course/assignment/private/code',
    `level`       VARCHAR(10)           DEFAULT 'info'  COMMENT '级别：info/warn/danger/success',
    `title`       VARCHAR(200) NOT NULL                COMMENT '标题',
    `content`     TEXT                                  COMMENT '正文',
    `brief`       VARCHAR(500)                          COMMENT '列表中显示的摘要（可由 content 截取）',
    `target_url`  VARCHAR(255)                          COMMENT '点击跳转地址',
    `read_flag`   TINYINT      NOT NULL DEFAULT 0      COMMENT '0未读 1已读',
    `read_time`   DATETIME                              DEFAULT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_unread` (`user_id`, `read_flag`),
    KEY `idx_user_create` (`user_id`, `create_time`)
) ENGINE = InnoDB COMMENT ='站内消息中心';

-- 给示例账号 1 (admin) / 1001 (teacher1) / 2001 (student1) 各塞几条种子数据
INSERT INTO `message_notify`(`id`, `user_id`, `type`, `level`, `title`, `content`, `brief`, `target_url`, `read_flag`) VALUES
(1, 1, 'system',     'info',    '系统升级通知',     '平台将于本周日凌晨 02:00-04:00 进行例行维护升级，请合理安排作业提交时间。', '平台将于本周日凌晨 02:00-04:00 进行例行维护升级。', '/message/notice', 0),
(2, 1, 'system',     'success', '课程资源审核通过', '您提交的《Java 并发编程》课程已通过审核并发布到门户。',                 '您提交的《Java 并发编程》课程已通过审核。',     '/portal/notice',  0),
(3, 1, 'system',     'warn',    '异常登录提醒',       '检测到您的账号在新设备登录，如非本人操作请及时修改密码。',               '检测到您的账号在新设备登录。',                  '/profile/security',1),
(4, 1001, 'course',  'info',    '新学生加入《Web应用开发》', '学生 王小明 已加入您的课程。', '学生 王小明 已加入您的课程。', '/course/manage', 0),
(5, 1001, 'assignment', 'success', '《期中作业》有 24 份新提交', '24 份新提交等待批改，请尽快处理。', '24 份新提交等待批改。', '/assignment/submission-list', 0),
(6, 1001, 'system',  'info',    '本周教研会议提醒',           '本周五下午 14:00 在会议室 A-301 召开教研会议。', '本周五下午 14:00 召开教研会议。', NULL, 1),
(7, 2001, 'course',  'info',    '新课程已发布：《Java 程序设计基础》', '主讲：李副教授 · 开课学期：2025-2026-1', '新课程已发布。', '/course/2', 0),
(8, 2001, 'assignment','warn',  '《实验三》剩余 3 天截止',     '请尽快提交，否则将影响平时成绩。', '剩余 3 天截止。', '/student/assignment/list', 0),
(9, 2001, 'private', 'success', '辅导员已通过你的请假申请',   '系统已更新你的请假状态。', '系统已更新你的请假状态。', '/profile/message', 1);
-- 既存库的迁移：把 sys_user 上的旧简单 UK 替换为"仅作用于未删除行"的函数式 UK
-- 新装库（fresh init.sql）不会重复执行，因为 DROP/CREATE 已经重建了 sys_user。
-- MySQL 8.0.16+ 才有 DROP INDEX IF EXISTS；8.0 整系列都支持函数式索引。
-- =====================================================================
-- 仅当旧索引还在时才执行 DROP / ADD，避免对新装库误报"重复键"
SET @stmt := IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE()
             AND TABLE_NAME = 'sys_user'
             AND INDEX_NAME = 'uk_username'),
    'ALTER TABLE `sys_user` DROP INDEX `uk_username`',
    'SELECT 1');
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

SET @stmt := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'sys_user'
                 AND INDEX_NAME = 'uk_username_active'),
    'ALTER TABLE `sys_user` ADD UNIQUE KEY `uk_username_active` ((CASE WHEN deleted = 0 THEN username END))',
    'SELECT 1');
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

-- =====================================================================
-- 结束
-- =====================================================================
SELECT '初始化完成' AS message;
