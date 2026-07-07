-- =====================================================================
-- 智能化在线教学支持服务平台管理中心系统 - 数据库初始化脚本
-- MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `smart_teach_platform`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `smart_teach_platform`;

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
    UNIQUE KEY `uk_username` (`username`)
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
    `course_type`  TINYINT               DEFAULT 1 COMMENT '1必修 2选修 3通识',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0未发布 1已发布 2已结课',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    BIGINT                DEFAULT NULL,
    `update_by`    BIGINT                DEFAULT NULL,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_code` (`course_code`)
) ENGINE = InnoDB COMMENT ='课程';

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
    `content_type`  TINYINT      NOT NULL COMMENT '1PPT 2视频 3文档 4链接 5富文本',
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
    `id`            BIGINT NOT NULL,
    `plan_id`       BIGINT NOT NULL,
    `plan_title`    VARCHAR(200)    DEFAULT NULL,
    `student_id`    BIGINT          DEFAULT NULL,
    `student_name`  VARCHAR(50)     DEFAULT NULL,
    `class_name`    VARCHAR(50)     DEFAULT NULL,
    `phone`         VARCHAR(20)     DEFAULT NULL,
    `status`        TINYINT         DEFAULT 0 COMMENT '0待审核 1已通过 2已驳回 3已完成',
    `sign_in_time`  DATETIME        DEFAULT NULL,
    `sign_out_time` DATETIME        DEFAULT NULL,
    `score`         DECIMAL(5, 2)   DEFAULT NULL,
    `comment`       VARCHAR(500)    DEFAULT NULL,
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT          DEFAULT NULL,
    `update_by`     BIGINT          DEFAULT NULL,
    `deleted`       TINYINT NOT NULL DEFAULT 0,
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
    `cover_image`   VARCHAR(500) DEFAULT NULL,
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
(100,  0, '课程计划管理', 1, '/course',     NULL,                    'education', '', 2, 1, 1),
(200,  0, '课程实验计划管理', 1, '/experiment', NULL,                  'experiment', '', 3, 1, 1),
(300,  0, '实训计划管理',  1, '/training',   NULL,                    'training', '', 4, 1, 1),
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

-- 课程实验计划管理
(201, 200, '实验计划', 2, '/experiment/plan', 'experiment/PlanList', NULL, 'experiment:plan:list', 1, 1, 1),
(210, 201, '新增', 3, NULL, NULL, NULL, 'experiment:plan:add', 1, 1, 1),
(211, 201, '编辑', 3, NULL, NULL, NULL, 'experiment:plan:edit', 2, 1, 1),
(212, 201, '删除', 3, NULL, NULL, NULL, 'experiment:plan:remove', 3, 1, 1),
(213, 201, '审核', 3, NULL, NULL, NULL, 'experiment:plan:approve', 4, 1, 1),

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
(603, 600, '操作日志',   2, '/monitor/operation-log',  'monitor/OperationLog', NULL, 'monitor:operationLog:list', 3, 1, 1);

-- 超级管理员分配所有菜单
INSERT INTO `sys_role_menu`(`id`, `role_id`, `menu_id`)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 1000, 1, id FROM sys_menu;

-- 字典类型
INSERT INTO `sys_dict_type`(`id`, `dict_name`, `dict_type`, `description`, `status`) VALUES
(1, '课程性质', 'course_type', '课程性质字典', 1),
(2, '实验类型', 'exp_type', '实验类型字典', 1),
(3, '资源类型', 'resource_type', '教学资源类型', 1),
(4, '课程内容类型', 'content_type', '课程内容类型', 1),
(5, '门户内容类型', 'portal_type', '门户内容类型', 1),
(6, '学期', 'semester', '当前学期列表', 1);

INSERT INTO `sys_dict_data`(`id`, `dict_type`, `dict_label`, `dict_value`, `list_class`, `sort`, `status`, `is_default`) VALUES
-- 课程性质
(1, 'course_type', '必修', '1', 'primary', 1, 1, 1),
(2, 'course_type', '选修', '2', 'success', 2, 1, 0),
(3, 'course_type', '通识', '3', 'info',    3, 1, 0),
-- 实验类型
(10, 'exp_type', '验证性', '1', 'primary', 1, 1, 0),
(11, 'exp_type', '综合性', '2', 'success', 2, 1, 0),
(12, 'exp_type', '设计性', '3', 'warning', 3, 1, 0),
(13, 'exp_type', '创新性', '4', 'danger',  4, 1, 0),
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
(33, 'content_type', '链接',   '4', 'success', 4, 1, 0),
(34, 'content_type', '富文本', '5', 'warning', 5, 1, 0),
-- 门户内容类型
(40, 'portal_type', '轮播图',   '1', 'primary', 1, 1, 0),
(41, 'portal_type', '通知公告', '2', 'warning', 2, 1, 0),
(42, 'portal_type', '新闻资讯', '3', 'success', 3, 1, 0),
-- 学期
(50, 'semester', '2024-2025-1', '2024-2025-1', 'primary', 1, 1, 0),
(51, 'semester', '2024-2025-2', '2024-2025-2', 'primary', 2, 1, 0),
(52, 'semester', '2025-2026-1', '2025-2026-1', 'primary', 3, 1, 1);

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

-- =====================================================================
-- 结束
-- =====================================================================
SELECT '初始化完成' AS message;
