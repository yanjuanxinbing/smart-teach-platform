import { createRouter, createWebHistory } from 'vue-router'
import PortalLayout from '@/layout/PortalLayout.vue'

const routes = [
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

router.afterEach((to) => {
  if (to.meta?.title) document.title = `${to.meta.title} · 智能教学平台`
  else document.title = '智能教学平台 · 教研支持'
})

export default router
