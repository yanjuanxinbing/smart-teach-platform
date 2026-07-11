import axios from 'axios'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/store/user'

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
  // silentError: true 时不弹全局错误 toast，由调用方自己接管并展示更友好的提示
  if (!response.config || !response.config.silentError) {
    ElMessage.error(res.message || '操作失败')
  }
  return Promise.reject(new Error(res.message || '操作失败'))
}, error => {
  NProgress.done()
  if (!error.config || !error.config.silentError) {
    ElMessage.error(error.message || '网络异常')
  }
  return Promise.reject(error)
})

export default service