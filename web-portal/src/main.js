import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { useUserStore } from '@/store/user'

import 'element-plus/dist/index.css'
import '@/styles/index.scss'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')

// 应用启动水合:有 token 但 roles 为空(如带旧 token 首次进站/持久化数据缺失)时,
// 主动向后端拉一次用户信息回填 roles,让「我的学习」等角色相关入口立即显示,
// 而不必先访问 /profile 才触发 fetchUserInfo。持久化的 roles 已能即时渲染,
// 这里再刷新一次以后端为权威来源,防止持久化数据过期。
{
  const bootUserStore = useUserStore()
  if (bootUserStore.isLogin && !bootUserStore.roles.length) {
    bootUserStore.fetchUserInfo().catch(() => {})
  }
}

// 全局 401 清理钩子 —— request.js 在业务 401 时 dispatch 'portal:auth-expired',
// 这里接住事件并把 store.userInfo / roles / permissions 一并清空,
// 避免头部 / 个人中心在 token 失效后仍展示僵尸用户态。
// （request.js 不直接 import store 以避免循环依赖）
window.addEventListener('portal:auth-expired', () => {
  const userStore = useUserStore()
  if (userStore.userInfo || userStore.roles.length) {
    userStore._clearLocal()
  }
})
