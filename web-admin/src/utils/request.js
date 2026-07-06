import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/store/user'
import router from '@/router'

NProgress.configure({ showSpinner: false })

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
    ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    })
    return Promise.reject(new Error(res.message))
  }
  ElMessage.error(res.message || '操作失败')
  return Promise.reject(new Error(res.message || '操作失败'))
}, error => {
  NProgress.done()
  ElMessage.error(error.message || '网络异常')
  return Promise.reject(error)
})

export default service
