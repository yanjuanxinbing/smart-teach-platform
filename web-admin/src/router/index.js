import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册账号', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      // 课程计划管理
      { path: 'course/manage', name: 'CourseManage', component: () => import('@/views/course/CourseList.vue'), meta: { title: '课程管理', icon: 'Notebook' } },
      { path: 'course/chapter', name: 'CourseChapter', component: () => import('@/views/course/ChapterList.vue'), meta: { title: '章节与内容', icon: 'List' } },
      { path: 'course/plan', name: 'CoursePlan', component: () => import('@/views/course/PlanList.vue'), meta: { title: '教学计划', icon: 'Calendar' } },
      { path: 'course/teach', name: 'CourseTeach', component: () => import('@/views/course/TeachList.vue'), meta: { title: '授课管理', icon: 'UserFilled' } },
      // 课程实验计划管理
      { path: 'experiment/plan', name: 'ExperimentPlan', component: () => import('@/views/experiment/PlanList.vue'), meta: { title: '实验计划', icon: 'Cpu' } },
      // 实训计划管理
      { path: 'training/plan', name: 'TrainingPlan', component: () => import('@/views/training/PlanList.vue'), meta: { title: '实训计划', icon: 'Promotion' } },
      { path: 'training/registration', name: 'TrainingRegistration', component: () => import('@/views/training/RegistrationList.vue'), meta: { title: '报名管理', icon: 'UserFilled' } },
      // 作业管理（顶级菜单由后端 my-menu 控制；此处按角色筛路由即可）
      { path: 'assignment/list', name: 'AssignmentList', component: () => import('@/views/course/AssignmentList.vue'), meta: { title: '作业列表', icon: 'Notebook' } },
      { path: 'assignment/submission-list', name: 'SubmissionList', component: () => import('@/views/course/SubmissionList.vue'), meta: { title: '提交批改', icon: 'EditPen' } },
      { path: 'student/assignment/list', name: 'StudentAssignmentList', component: () => import('@/views/student/MyAssignmentList.vue'), meta: { title: '我的作业', icon: 'Reading' } },
      // 资源管理
      { path: 'resource/category', name: 'ResourceCategory', component: () => import('@/views/resource/CategoryList.vue'), meta: { title: '资源分类', icon: 'FolderOpened' } },
      { path: 'resource/list', name: 'ResourceList', component: () => import('@/views/resource/ResourceList.vue'), meta: { title: '资源列表', icon: 'Files' } },
      // 网站门户
      { path: 'portal/banner', name: 'PortalBanner', component: () => import('@/views/portal/BannerList.vue'), meta: { title: '轮播图', icon: 'Picture' } },
      { path: 'portal/notice', name: 'PortalNotice', component: () => import('@/views/portal/NoticeList.vue'), meta: { title: '通知公告', icon: 'Bell' } },
      { path: 'portal/news', name: 'PortalNews', component: () => import('@/views/portal/NewsList.vue'), meta: { title: '新闻资讯', icon: 'Document' } },
      // 系统管理
      { path: 'system/user', name: 'SystemUser', component: () => import('@/views/system/UserList.vue'), meta: { title: '用户管理', icon: 'User' } },
      { path: 'system/dept', name: 'SystemDept', component: () => import('@/views/system/DeptList.vue'), meta: { title: '部门管理', icon: 'OfficeBuilding' } },
      { path: 'system/role', name: 'SystemRole', component: () => import('@/views/system/RoleList.vue'), meta: { title: '角色管理', icon: 'UserFilled' } },
      { path: 'system/menu', name: 'SystemMenu', component: () => import('@/views/system/MenuList.vue'), meta: { title: '菜单管理', icon: 'Menu' } },
      { path: 'system/dict', name: 'SystemDict', component: () => import('@/views/system/DictList.vue'), meta: { title: '字典管理', icon: 'Collection' } },
      { path: 'system/config', name: 'SystemConfig', component: () => import('@/views/system/ConfigList.vue'), meta: { title: '参数设置', icon: 'Tools' } },
      {
        path: 'system/class',
        name: 'SystemClass',
        component: () => import('@/views/system/ClassList.vue'),
        meta: { title: '班级管理', icon: 'User' }
      },
      // 系统监控
      { path: 'monitor/server', name: 'MonitorServer', component: () => import('@/views/monitor/Server.vue'), meta: { title: '服务器监控', icon: 'Monitor' } },
      { path: 'monitor/login-log', name: 'MonitorLoginLog', component: () => import('@/views/monitor/LoginLog.vue'), meta: { title: '登录日志', icon: 'Login' } },
      { path: 'monitor/operation-log', name: 'MonitorOperationLog', component: () => import('@/views/monitor/OperationLog.vue'), meta: { title: '操作日志', icon: 'Operation' } },
      // 消息中心
      { path: 'message/notice', name: 'MessageNotice', component: () => import('@/views/message/NoticeList.vue'), meta: { title: '系统通知', icon: 'Bell' } },
      { path: 'message/private', name: 'MessagePrivate', component: () => import('@/views/message/PrivateMsg.vue'), meta: { title: '站内私信', icon: 'ChatLineSquare' } },
      // 个人中心（从头像菜单进入）
      { path: 'profile/index',    name: 'ProfileIndex',    component: () => import('@/views/profile/Index.vue'),    meta: { title: '我的资料', icon: 'User',       hiddenInMenu: true } },
      { path: 'profile/security', name: 'ProfileSecurity', component: () => import('@/views/profile/Security.vue'), meta: { title: '安全设置', icon: 'Lock',       hiddenInMenu: true } },
      { path: 'profile/log',      name: 'ProfileLog',      component: () => import('@/views/profile/Log.vue'),      meta: { title: '我的日志', icon: 'Operation',  hiddenInMenu: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = (to.meta.title || '') + ' - 智能教学平台管理中心'
  const userStore = useUserStore()
  if (to.meta.public) {
    next()
  } else if (!userStore.token) {
    next('/login')
  } else {
    next()
  }
  NProgress.done()
})

export default router
