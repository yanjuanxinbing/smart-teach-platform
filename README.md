# 智能化在线教学支持服务平台

> 前后端分离的智能化在线教学支持服务平台（后台管理中心 + 学生学习门户）  
> Spring Boot 2.7 + MyBatis-Plus + Spring Security + JWT + 本地 Ollama AI  │  Vue 3 + Element Plus + Vite

## 📖 项目简介

本项目为高校/培训机构的**智能化在线教学支持服务平台**，采用「一后端 + 双前端」架构：

- **web-admin（后台管理中心，教师/管理员）**：课程计划、课程实验计划、实训计划、作业布置与批改、作业 AI 分析、资源管理、班级管理、用户/权限/部门/字典/参数管理、系统监控（服务器 + 登录/操作日志）、消息通知、门户内容维护等。
- **web-portal（学习门户，学生）**：注册/登录、浏览与选修课程、查看章节内容与文档、在线提交作业并查看批改、实验/实训报名与查看、资源下载、代码片段（Codex）浏览、学习笔记、**AI 解题助手**、个人中心（资料/安全/消息/日志）等。

系统基于 RBAC 权限模型，使用 JWT 实现无状态认证；后端使用 MyBatis-Plus + 分页插件，AI 能力基于本地 **Ollama（gemma3:1b）**，前端使用 Vue 3 + Element Plus，按业务领域拆分子模块，便于二次开发与扩展。

## 🏗 技术栈

### 后端
- **基础框架**：Spring Boot 2.7.18、Java 1.8
- **持久层**：MyBatis-Plus 3.5.3.1、分页插件、逻辑删除、自动填充
- **安全**：Spring Security + JWT（jjwt 0.9.1）
- **工具库**：Lombok、Fastjson2、Commons-Lang3、Commons-IO
- **系统监控**：Oshi（跨平台 CPU/内存/磁盘/JVM 信息）
- **AI 能力**：本地 Ollama（默认 `gemma3:1b`）—— AI 解题助手（学生端）+ 作业 AI 分析（后台）
- **异步/定时**：Spring `@Async`（操作日志）+ `@Scheduled`（作业自动截止）
- **缓存**：Redis
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
│   │   │       ├── auth/         # 认证（登录/登出/注册/当前用户/密码初始化）
│   │   │       ├── user/         # 用户管理
│   │   │       ├── permission/   # 角色/菜单/权限
│   │   │       ├── system/       # 部门/字典/参数配置/班级(sys_class)
│   │   │       ├── portal/       # 门户 + 学生学习端接口（文章/课程/实训/我的学习）
│   │   │       ├── course/       # 课程计划：course/chapter/content/plan/teacher
│   │   │       │   └── assignment/  # 作业：布置/提交/批改 + 自动截止定时任务
│   │   │       ├── experiment/   # 课程实验计划 + 实验作业
│   │   │       ├── training/     # 实训计划 + 报名/签到/成绩
│   │   │       ├── resource/     # 资源管理：分类 + 资源 + 文件上传 + 前台资源接口
│   │   │       ├── aiassignment/ # 作业 AI 分析（班级/学生维度报告，后台）
│   │   │       ├── aiassistant/  # AI 解题助手（Ollama 会话，学生端）
│   │   │       ├── codex/        # 代码片段（Codex）浏览/分享
│   │   │       ├── message/      # 消息通知（站内通知 + 私信）
│   │   │       ├── profile/      # 个人中心（改资料/改密码/我的日志）
│   │   │       └── systemmonitor/# 系统监控 + 仪表盘：服务器 + 登录日志 + 操作日志
│   │   └── resources/
│   │       ├── application.yaml                    # 应用配置（含 Ollama AI 配置）
│   │       └── mapper/                              # MyBatis XML（用户/菜单/统计自定义SQL）
│   └── test/                                       # 测试用例
│
├── web-admin/                              # 前端 - 后台管理中心（端口 8081，教师/管理员）
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── api/                                   # 各模块API封装
│       │   ├── auth.js  system.js  course.js  experiment.js  training.js
│       │   ├── resource.js  portal.js  monitor.js  analytics.js
│       │   ├── assignment.js  teaching.js  profile.js
│       ├── components/Pagination.vue
│       ├── hooks/useDict.js                       # 数据字典 hook
│       ├── layout/                                # 主布局
│       ├── router/index.js                        # 路由
│       ├── store/user.js                          # Pinia 用户 store
│       ├── styles/index.scss
│       ├── utils/request.js                       # Axios 封装
│       └── views/                                 # 页面
│           ├── Login.vue  Register.vue  Dashboard.vue
│           ├── course/       (CourseList / ChapterList / PlanList / TeachList /
│           │                  AssignmentList / SubmissionList / AssignmentAnalyticsClass)
│           ├── experiment/   (PlanList / AssignmentList)
│           ├── training/     (PlanList / RegistrationList)
│           ├── resource/     (ResourceList / CategoryList)
│           ├── student/      (MyAssignmentList / AssignmentAnalyticsStudent)
│           ├── message/      (NoticeList / PrivateMsg)
│           ├── profile/      (Index / Security / Log)
│           ├── portal/       (BannerList / NoticeList / NewsList)
│           ├── system/       (UserList / RoleList / MenuList / DeptList / DictList / ConfigList / ClassList)
│           └── monitor/      (Server / LoginLog / OperationLog)
│
└── web-portal/                             # 前端 - 学生学习门户（端口 8082）
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/          (auth / portal / course / experiment / training / resource /
        │                  document / my / profile / codex / aiassistant)
        ├── router/index.js
        ├── styles/index.scss
        ├── utils/request.js
        ├── views/
        │   ├── Home.vue  Login.vue  Register.vue        # 首页 / 登录 / 注册
        │   ├── Notice.vue  News.vue  Article.vue        # 通知 / 新闻 / 详情
        │   ├── CourseList.vue  CourseDetail.vue  DocumentView.vue   # 课程浏览 / 章节文档
        │   ├── ExperimentDetail.vue  TrainingList.vue  TrainingDetail.vue
        │   ├── AssignmentSubmit.vue  AssignmentSubmission.vue  AssignmentGrade.vue  # 作业提交/批改结果
        │   ├── MyCourses.vue  MyExperiments.vue  MyTrainings.vue        # 我的学习
        │   ├── MyResources.vue  MyAssignments.vue  StudentNotes.vue
        │   ├── CodeList.vue  CodeDetail.vue             # 代码片段
        │   ├── AiAssistant.vue                          # AI 解题助手
        │   ├── Stats.vue                                # 学习统计
        │   └── ProfileIndex.vue  ProfileSecurity.vue  ProfileMessage.vue  ProfileLog.vue
        └── assets/banner.svg
```

## 🚀 快速开始

### 1. 环境要求
- JDK 1.8+
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

### 6.（可选）启用 AI 能力（Ollama）
学生端「AI 解题助手」与后台「作业 AI 分析」依赖本地 [Ollama](https://ollama.com)：
```bash
# 安装 Ollama 后拉取默认小模型（1B，本地 8GB 内存即可运行）
ollama pull gemma3:1b
ollama serve   # 默认监听 http://localhost:11434
```
模型、超时、提示词等均可在 [application.yaml](src/main/resources/application.yaml) 的 `aiassistant` / `aiassignment` 配置项中调整。未启动 Ollama 时不影响其它功能，仅 AI 相关接口不可用。

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

### 5. 作业管理（后台布置 + 学生提交批改）
- 教师在课程下**布置作业**（标题、要求、附件、满分、开始/截止时间），可**按班级定向发布**（`assignment_target_class`）
- 学生在学习门户**在线提交**（正文 + 附件），教师在后台**批改打分 + 评语**
- 到达截止时间由定时任务 `AssignmentAutoCloseTask` **自动关闭**并标记未交
- **课程实验作业**（`experiment_assignment`）同套提交/批改流程

### 6. 作业 AI 分析（aiassignment，后台）
- 基于提交与成绩数据，调用本地 Ollama 生成**班级维度 / 学生维度**的分析报告
- 班级：平均分、成绩分布、按月趋势、未提交清单等统计 + AI 文字点评
- 学生：个人得分趋势与薄弱点分析，辅助教师因材施教

### 7. AI 解题助手（aiassistant，学生端）
- 学生端集成 **AI 解题助手**（本地 Ollama `gemma3:1b`），多轮会话、上下文记忆（默认保留最近 8 轮）
- 定位为"引导思路"而非"代做作业"，系统提示词可在 yaml 中热调整
- 会话/消息持久化（`aiassistant_session` / `aiassistant_message`），并对并发做 Semaphore 限流

### 8. 代码片段（Codex）
- 代码片段库（`codex_snippet`），学生端可浏览/查看，用于教学示例与代码分享

### 9. 消息通知
- **站内通知**：面向用户群发/单发的系统通知
- **私信**：用户间点对点消息
- 未读计数、标记已读，学生端与后台均可查看

### 10. 资源管理
- **资源分类**：树形分类（顶级 → 二级 → ...）
- **资源列表**：上传文件、维护信息（名称、类型、所属分类、关联课程、标签、描述）、上下架
- 支持**预览/下载**，自动累计浏览/下载次数；学生端可检索下载
- 文件存储：默认本地磁盘，可在 [application.yaml](src/main/resources/application.yaml) 中切换为 OSS / MinIO

### 11. 系统管理
- **用户管理**：账号、密码、姓名、角色分配、启/禁用、重置密码
- **部门管理**：树形部门
- **班级管理**：班级（`sys_class`）+ 学生-班级关联（`sys_user_class`），作业定向发布与选课的基础
- **角色管理**：角色 + 分配菜单/按钮权限（前端控制菜单显示 + 后端 `@PreAuthorize` 鉴权）
- **菜单管理**：三级（目录 / 菜单 / 按钮）+ 权限标识
- **字典管理**：字典类型 + 字典数据（前端通过 `useDict('xxx')` hook 自动加载）
- **参数设置**：键值对系统配置（系统内置参数不可删/改）

### 12. 个人中心
- 修改个人资料、修改密码、查看**我的操作日志**（学生端与后台均有）

### 13. 系统监控与仪表盘
- **仪表盘**：`DashboardController` 汇总课程/用户/作业等关键指标供首页展示
- **服务器监控**：实时显示 CPU（使用率/核数）、内存、JVM、磁盘、系统信息，10秒自动刷新
- **登录日志**：记录每次登录（账号、IP、浏览器、操作系统、状态、消息）
- **操作日志**：通过 `@OperationLog` 注解 + AOP 自动采集（模块、操作、URI、参数、返回、IP、用户、耗时、状态），支持清理/批量删除

### 14. 学生学习门户（web-portal）
- **注册/登录**：学生自助注册账号
- **选课与学习**：浏览课程、选修（`course_enrollment`）、查看章节内容与文档
- **我的学习**：我的课程 / 实验 / 实训 / 资源 / 作业、学习笔记、学习统计
- 集成上述 **AI 解题助手、代码片段、消息、个人中心**

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

**Q: AI 解题助手 / 作业 AI 分析报错或无响应？**  
A: 确认已启动本地 Ollama（`ollama serve`）并拉取了模型（`ollama pull gemma3:1b`）；检查 `application.yaml` 中 `aiassistant.ollama.base-url` 是否指向 `http://localhost:11434`。首字延迟较高属正常，可用更小/量化模型或调大 `timeout`。

## 📄 License
仅用于教学/实训用途。
