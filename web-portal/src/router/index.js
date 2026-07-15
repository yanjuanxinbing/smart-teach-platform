import { createRouter, createWebHistory } from 'vue-router'
import PortalLayout from '@/layout/PortalLayout.vue'

const routes = [
  // 公开路由（无侧栏 / 无底部），放在根级避免被 PortalLayout 包住
  { path: '/login',    name: 'Login',    component: () => import('@/views/Login.vue'),    meta: { title: '账号登录', public: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { title: '账号注册', public: true } },
  {
    path: '/',
    component: PortalLayout,
    children: [
      { path: '',        name: 'Home',     component: () => import('@/views/Home.vue') },
      { path: 'course',  name: 'CourseList', component: () => import('@/views/CourseList.vue') },
      { path: 'course/:id', name: 'CourseDetail', component: () => import('@/views/CourseDetail.vue') },
      { path: 'codex',   name: 'CodeList',  component: () => import('@/views/CodeList.vue') },
      { path: 'codex/:id', name: 'CodeDetail', component: () => import('@/views/CodeDetail.vue') },
      { path: 'notice',  name: 'Notice',    component: () => import('@/views/Notice.vue') },
      { path: 'news',    name: 'News',      component: () => import('@/views/News.vue') },
      { path: 'article/:id', name: 'Article', component: () => import('@/views/Article.vue') },
      { path: 'stats',   name: 'Stats',     component: () => import('@/views/Stats.vue') },

      // 个人中心（嵌套布局，登录态由 store/user.js 内部判断）
      {
        path: 'profile',
        component: () => import('@/layout/ProfileLayout.vue'),
        children: [
          { path: '',          name: 'ProfileIndex',    component: () => import('@/views/ProfileIndex.vue') },
          { path: 'security', name: 'ProfileSecurity', component: () => import('@/views/ProfileSecurity.vue') },
          { path: 'message',  name: 'ProfileMessage',  component: () => import('@/views/ProfileMessage.vue') },
          { path: 'log',      name: 'ProfileLog',      component: () => import('@/views/ProfileLog.vue') }
        ]
      },

      // 学生学习中心:受保护 (router.beforeEach 中 startsWith('/my') 拦截, 仅 STUDENT 角色可入)
      // TODO: [实训详情页] [P0] /training/:id 详情页路由缺失;现 MyTrainings.vue 的卡片点击仅 ElMessage 提示"开发中". 建议新增 views/TrainingDetail.vue 并在 MyTrainings.vue 的 goDetail() 中 router.push('/training/' + t.trainingId).
      // TODO: [作业提交 / 提交查看 / 批改查看页] [P0] MyAssignments.vue 三个按钮"去提交 / 查看提交 / 查看批改"目前没有对应路由. 需要:
      //       /portal/assignment/:id/submit     -> 学生提交表单 (含富文本/附件上传)
      //       /portal/assignment/:id/submission -> 已提交内容展示
      //       /portal/assignment/:id/grade      -> 老师批改结果展示 (学生视角)
      // 后端契约见 web-portal/src/api/my.js
      {
        path: 'my',
        component: () => import('@/layout/MyLearningLayout.vue'),
        children: [
          { path: 'courses',     name: 'MyCourses',     component: () => import('@/views/MyCourses.vue'),     meta: { title: '我的课程' } },
          { path: 'assignments', name: 'MyAssignments', component: () => import('@/views/MyAssignments.vue'), meta: { title: '我的作业' } },
          { path: 'trainings',   name: 'MyTrainings',   component: () => import('@/views/MyTrainings.vue'),   meta: { title: '我的实训' } },
          { path: '',            redirect: '/my/courses' }
        ]
      },

      // 兼容 /message → 个人中心-消息
      { path: 'message', redirect: '/profile/message' }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, saved) { if (saved) return saved; return { top: 0, behavior: 'smooth' } }
})

/**
 * 全局前置路由守卫（**仅此一处**，不再加 beforeResolve / 全局后置守卫做权限校验）。
 *
 * 设计要点：
 *  1. /login 与 /register 永远是白名单，写在最前面，确保：
 *      - 已登录用户不会因 token 过期被踢回 /login 后陷入死循环；
 *      - 未登录用户访问 /login 不会被自身的 guard 再挡一次。
 *  2. /profile/* 与 /my/* 是受保护路径：缺少 portal_token 时跳 /login?redirect=<原路径>。
 *     /my/* 额外要求角色为 STUDENT —— 教师/管理员误入会被引导回首页，避免后台误用前台壳。
 *  3. isNavigating 防重复执行锁：避免在短时间内（同一逻辑帧）
 *     因为组件跳转 / 嵌套 push 触发 guard 二次进入。
 *  4. 接口是否需要登录由后端 JwtAuthenticationFilter 真正决定；前端 guard
 *     只做"明显不该让游客看的页面"的快速拦截，不影响普通浏览页（首页/课程/笔记/资讯 等）。
 */
import { useUserStore } from '@/store/user'

let isNavigating = false

router.beforeEach((to, from, next) => {
  // (1) 白名单放行 —— 必须在所有权限校验之前
  if (to.path === '/login' || to.path === '/register' || to.meta?.public) {
    return next()
  }

  // (2) 防重复执行锁：同一逻辑帧内若有重复进入（例如二级组件/嵌套 push），直接放行 next
  if (isNavigating) return next()
  isNavigating = true

  // (3) 受保护路径：/profile/* 与 /my/* 都需要登录
  if (to.path.startsWith('/profile') || to.path.startsWith('/my')) {
    const token = localStorage.getItem('portal_token')
    if (!token) {
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }

  // (3.5) /my/* 仅 STUDENT 角色可入：教师/管理员进来直接回首页
  if (to.path.startsWith('/my')) {
    const userStore = useUserStore()
    if (userStore.roleCode !== 'STUDENT') {
      return next({ path: '/', query: { denied: 'my' } })
    }
  }

  next()

  // (4) 当前 tick 结束后解锁，避免锁住下一帧导航
  setTimeout(() => { isNavigating = false }, 0)
})

router.afterEach((to) => {
  if (to.meta?.title) document.title = `${to.meta.title} · 智能教学平台`
  else document.title = '智能教学平台 · 教研支持'
})

export default router
