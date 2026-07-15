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
