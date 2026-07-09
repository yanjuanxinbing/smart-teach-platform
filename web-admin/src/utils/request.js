import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/store/user'
import router from '@/router'

NProgress.configure({ showSpinner: false })

// 401 弹窗幂等保护：避免多个并发 401 响应连续弹出多个「登录过期」窗口
let isHandling401 = false

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

service.interceptors.request.use(config => {
  NProgress.start()
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers['Authorization'] = 'Bearer ' + userStore.token
  }
  return config
})

service.interceptors.response.use(response => {
  NProgress.done()
  const res = response.data
  if (res.code === 200) {
    return res.data
  }
  if (res.code === 401) {
    // 如果已经在处理 401（弹窗已弹出），后续 401 直接 reject，不再叠加弹窗
    if (isHandling401) {
      return Promise.reject(new Error(res.message || '登录状态已过期'))
    }
    isHandling401 = true
    ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }).catch(() => {
      // 用户点「取消」或关闭弹窗，仅重置标志位，保留在原页面
    }).finally(() => {
      isHandling401 = false
    })
    return Promise.reject(new Error(res.message || '登录状态已过期'))
  }
  ElMessage.error(res.message || '操作失败')
  return Promise.reject(new Error(res.message || '操作失败'))
}, error => {
  NProgress.done()
  ElMessage.error(error.message || '网络异常')
  return Promise.reject(error)
})

export default service
