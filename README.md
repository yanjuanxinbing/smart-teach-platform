# 智能化在线教学支持服务平台管理中心系统

> 前后端分离的智能化在线教学支持服务平台后端管理系统  
> Spring Boot 2.7 + MyBatis-Plus + Spring Security + JWT  │  Vue 3 + Element Plus + Vite

## 📖 项目简介

本项目为高校/培训机构智能化在线教学支持服务平台的管理中心系统，覆盖**课程计划管理、课程实验计划管理、实训计划管理、资源管理、用户/权限/部门/字典/参数管理、系统监控（服务器 + 登录日志 + 操作日志）**、**网站门户**等核心业务模块。  
系统基于 RBAC 权限模型，使用 JWT 实现无状态认证；后端使用 MyBatis-Plus + 分页插件，前端使用 Vue 3 + Element Plus，按业务领域拆分子模块，便于二次开发与扩展。

## 🏗 技术栈

### 后端
- **基础框架**：Spring Boot 2.7.18、Java 1.8
- **持久层**：MyBatis-Plus 3.5.3.1、分页插件、逻辑删除、自动填充
- **安全**：Spring Security + JWT（jjwt 0.9.1）
- **工具库**：Lombok、Fastjson2、Commons-Lang3、Commons-IO
- **系统监控**：Oshi（跨平台 CPU/内存/磁盘/JVM 信息）
- **接口文档**：Knife4j 4.3（Swagger UI 增强）
- **数据库**：MySQL 8.0
- **构建**：Maven

### 前端
- **基础框架**：Vue 3.4 + Vite 4 + Vue Router 4 + Pinia
- **UI 组件**：Element Plus 2.4
- **HTTP**：Axios + 拦截器（token注入、统一错误处理）
- **图表**：ECharts 5 + vue-echarts
- **工具**：js-cookie、dayjs、nprogress

## 📁 项目结构

```
smart-teach-platform/
├── pom.xml                                 # 后端Maven配置
├── sql/
│   └── init.sql                            # 数据库初始化脚本（建表+种子数据）
├── src/                                    # 后端源码
│   ├── main/
│   │   ├── java/com/smartteach/
│   │   │   ├── SmartTeachPlatformApplication.java   # 启动类
│   │   │   ├── common/                              # 通用：Result、异常、工具类
│   │   │   │   ├── base/    (BaseEntity/PageQuery/PageResult)
│   │   │   │   ├── result/  (Result/ResultCode)
│   │   │   │   ├── exception/ (BusinessException/GlobalExceptionHandler)
│   │   │   │   └── utils/   (JwtUtil/UserContext)
│   │   │   ├── config/                              # Spring配置：Security/CORS/上传/MVC
│   │   │   └── modules/                            # 业务模块
│   │   │       ├── auth/         # 认证（登录/登出/当前用户）
│   │   │       ├── user/         # 用户管理
│   │   │       ├── permission/   # 角色/菜单/权限
│   │   │       ├── system/       # 部门/字典/参数配置
│   │   │       ├── portal/       # 网站门户（前台公开接口 + 后台管理）
│   │   │       ├── course/       # 课程计划：course/chapter/content/plan
│   │   │       ├── experiment/   # 课程实验计划
│   │   │       ├── training/     # 实训计划 + 报名/签到/成绩
│   │   │       ├── resource/     # 资源管理：分类 + 资源 + 文件上传
│   │   │       └── systemmonitor/# 系统监控：服务器 + 登录日志 + 操作日志
│   │   └── resources/
│   │       ├── application.yaml                    # 应用配置
│   │       └── mapper/                              # MyBatis XML（用户/菜单自定义SQL）
│   └── test/                                       # 测试用例
│
├── web-admin/                              # 前端 - 后台管理中心（端口 8081）
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── api/                                   # 各模块API封装
│       │   ├── auth.js  system.js  course.js  experiment.js
│       │   ├── training.js  resource.js  portal.js  monitor.js
│       ├── components/Pagination.vue
│       ├── hooks/useDict.js                       # 数据字典 hook
│       ├── layout/                                # 主布局
│       │   ├── Index.vue  MenuItem.vue  Breadcrumb.vue  ChangePasswordDialog.vue
│       ├── router/index.js                        # 路由
│       ├── store/user.js                          # Pinia 用户 store
│       ├── styles/index.scss
│       ├── utils/request.js                       # Axios 封装
│       └── views/                                 # 页面
│           ├── Login.vue  Dashboard.vue
│           ├── course/       (CourseList / ChapterList / PlanList)
│           ├── experiment/   (PlanList)
│           ├── training/     (PlanList / RegistrationList)
│           ├── resource/     (ResourceList / CategoryList)
│           ├── portal/       (BannerList / NoticeList / NewsList)
│           ├── system/       (UserList / RoleList / MenuList / DeptList / DictList / ConfigList)
│           └── monitor/      (Server / LoginLog / OperationLog)
│
└── web-portal/                             # 前端 - 公开门户（端口 8082）
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/portal.js
        ├── router/index.js
        ├── styles/index.scss
        ├── utils/request.js
        ├── views/
        │   ├── Home.vue                           # 门户首页
        │   ├── Notice.vue                         # 通知公告
        │   ├── News.vue                           # 新闻资讯
        │   └── Article.vue                        # 详情页
        └── assets/banner.svg
```

## 🚀 快速开始

### 1. 环境要求
- JDK 21+
- Maven 3.6+
- Node.js 16+ & npm 8+
- MySQL 8.0+
- （可选）Redis 6.0+

### 2. 初始化数据库
```bash
# 登录MySQL后执行
mysql -u root -p < sql/init.sql
```
脚本会创建 `smart_teach_platform` 数据库及所有表，并插入默认账号、角色、菜单、字典、门户示例数据等。

### 3. 启动后端
```bash
# 在项目根目录
mvn spring-boot:run
# 或先打包再运行
mvn clean package -DskipTests
java -jar target/smart-teach-platform.jar
```
后端启动后：
- 接口地址：`http://localhost:8080/api`
- 接口文档（Knife4j）：`http://localhost:8080/api/doc.html`

### 4. 启动后台管理前端
```bash
cd web-admin
npm install
npm run dev
```
访问：`http://localhost:8081`  
默认账号：`admin` / `admin123`

### 5. 启动前台门户
```bash
cd web-portal
npm install
npm run dev
```
访问：`http://localhost:8082`

## 🔐 默认账号

| 账号 | 密码 | 角色 | 说明 |
|------|------|------|------|
| admin | admin | 超级管理员 | 拥有所有权限 |

> 数据库 `sys_user` 表里的默认密码是 BCrypt 加密后的 `admin`，登录后请到"个人信息 → 修改密码"自行修改为强密码。

## 📚 功能模块说明

### 1. 网站门户
- 后台可维护**轮播图、通知公告、新闻资讯**（草稿/发布/下线三种状态）
- 前台门户首页自动按类型拉取已发布内容展示
- 公开接口（`/portal/site/**`）无需鉴权

### 2. 课程计划管理
- **课程**：维护课程基本信息（编号、名称、学分、学时、性质、任课教师、封面、简介、状态）
- **章节与内容**：课程下树形章节，章节下挂载课件 PPT / 视频 / 文档 / 链接 / 富文本等内容
- **教学计划**：按"周次"拆解教学任务，绑定到一门课程和一个学期/班级，支持**提交 → 审核通过/驳回**流程

### 3. 课程实验计划管理
- 维护实验计划（标题、课程、学期、班级、教师、实验室、时间、次数、学时）
- 按**实验序号**拆分单次实验（实验名称、类型、目的、内容、要求、上课日期、节次等）
- 同样支持**草稿 → 提交 → 审核**流程

### 4. 实训计划管理
- 维护实训计划（项目名称、地点、时间、人数、目标、内容、考核方式）
- **报名管理**：学生报名、审核、签到/签退、登记成绩
- 状态机：草稿 → 已发布 → 进行中 → 已结束（/驳回）

### 5. 资源管理
- **资源分类**：树形分类（顶级 → 二级 → ...）
- **资源列表**：上传文件、维护信息（名称、类型、所属分类、关联课程、标签、描述）、上下架
- 支持**预览/下载**，自动累计浏览/下载次数
- 文件存储：默认本地磁盘，可在 [application.yaml](src/main/resources/application.yaml) 中切换为 OSS / MinIO

### 6. 系统管理
- **用户管理**：账号、密码、姓名、角色分配、启/禁用、重置密码
- **部门管理**：树形部门
- **角色管理**：角色 + 分配菜单/按钮权限（前端控制菜单显示 + 后端 `@PreAuthorize` 鉴权）
- **菜单管理**：三级（目录 / 菜单 / 按钮）+ 权限标识
- **字典管理**：字典类型 + 字典数据（前端通过 `useDict('xxx')` hook 自动加载）
- **参数设置**：键值对系统配置（系统内置参数不可删/改）

### 7. 系统监控
- **服务器监控**：实时显示 CPU（使用率/核数）、内存、JVM、磁盘、系统信息，10秒自动刷新
- **登录日志**：记录每次登录（账号、IP、浏览器、操作系统、状态、消息）
- **操作日志**：通过 `@OperationLog` 注解 + AOP 自动采集（模块、操作、URI、参数、返回、IP、用户、耗时、状态），支持清理/批量删除

## 🔑 权限模型

- 菜单/按钮表 `sys_menu.permission` 是权限标识（如 `course:list` / `course:edit`）
- 用户登录后 `JwtAuthenticationFilter` 解析 token 并把当前用户的权限集合写入 `SecurityContext`
- Controller 上通过 `@PreAuthorize("hasAuthority('course:list')")` 拦截
- 角色（`sys_role.role_code`）以 `ROLE_` 开头时，Spring Security 会自动识别为 GrantedAuthority
- `sys_user` + `sys_user_role` + `sys_role` + `sys_role_menu` + `sys_menu` 五张表实现完整 RBAC

## 🛠 扩展指南

### 新增业务模块
1. 在 `src/main/java/com/smartteach/modules/<module>/` 下创建标准的 `entity / dto / vo / mapper / service / controller` 目录
2. entity 继承 `BaseEntity` 自动获得 id/时间/创建人/逻辑删除
3. service 继承 `ServiceImpl<Mapper, Entity>` 获得 CRUD
4. controller 上加 `@PreAuthorize("hasAuthority('<module>:<action>')")`
5. 在 `sys_menu` 中插入菜单及权限记录，并把权限分配给角色

### 新增操作日志记录
在需要记录的接口方法上加注解：
```java
@OperationLog(module = "用户管理", action = "新增用户")
@PostMapping
public Result<Void> add(@RequestBody UserSaveDTO dto) { ... }
```

### 自定义登录过期/密码强度
- JWT 过期时间：`application.yaml` 中 `jwt.expire`（毫秒，默认12小时）
- 密码使用 BCrypt 加密，存储在 `sys_user.password`

## 📦 部署

### 后端打包
```bash
mvn clean package -DskipTests
# 上传 target/smart-teach-platform.jar 到服务器
nohup java -jar smart-teach-platform.jar --spring.profiles.active=prod > app.log 2>&1 &
```

### 前端打包
```bash
# 后台
cd web-admin && npm run build
# 前台
cd web-portal && npm run build
```
将 `dist/` 目录部署到 Nginx，配置反向代理 `/api` → `http://your-server:8080` 即可。

## 🧰 常见问题

**Q: Knife4j 接口文档看不到接口？**  
A: 检查 Controller 上是否加了 `@Api` 注解；接口路径需要登录（带 token）才能在右上角 Authorize 之后看到。

**Q: 启动报 MySQL 连接错误？**  
A: 修改 [application.yaml](src/main/resources/application.yaml) 中的 `spring.datasource.url/username/password`。

**Q: 上传文件 413 错误？**  
A: 调整 `spring.servlet.multipart.max-file-size`（默认 100MB）和 `max-request-size`（默认 200MB）。

**Q: 操作日志不记录？**  
A: 确认 `AsyncConfig` 已启用 `@EnableAsync`，并在方法上加了 `@OperationLog` 注解。

## 📄 License
仅用于教学/实训用途。
