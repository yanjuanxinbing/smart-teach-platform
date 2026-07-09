import { createRouter, createWebHistory } from 'vue-router'
import PortalLayout from '@/layout/PortalLayout.vue'

const routes = [
  {
    path: '/',
    component: PortalLayout,
    children: [
      { path: '',         name: 'Home',    component: () => import('@/views/Home.vue') },
      { path: 'notice',   name: 'Notice',  component: () => import('@/views/Notice.vue') },
      { path: 'news',     name: 'News',    component: () => import('@/views/News.vue') },
      { path: 'article/:id', name: 'Article', component: () => import('@/views/Article.vue') }
    ]
  }
]

export default createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, saved) {
    if (saved) return saved
    return { top: 0, behavior: 'smooth' }
  }
})
