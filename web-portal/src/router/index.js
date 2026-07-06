import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/notice', name: 'Notice', component: () => import('@/views/Notice.vue') },
  { path: '/news', name: 'News', component: () => import('@/views/News.vue') },
  { path: '/article/:id', name: 'Article', component: () => import('@/views/Article.vue') }
]

export default createRouter({ history: createWebHistory(), routes })
