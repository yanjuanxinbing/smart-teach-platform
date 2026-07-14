import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 前台门户 axios 实例。
 *
 * 关键点：
 *  - **请求拦截器统一注入 Authorization: Bearer <token>**（从 localStorage 读取），
 *    这是修复"登录后访问 /profile 触发 401"的核心。
 *  - 响应拦截器对同一错误消息做 DEDUP_WINDOW 去重；
 *    对业务 401 单点提示 + 单点清理 token，避免并发失败时一连串弹窗。
 *  - 调用方可通过 config.silentError = true 显式跳过 toast 与 401 处理
 *    （典型场景：logout 流程的 /auth/logout 即便 401 也不应弹错）。
 */

// ============ token 工具 ============
export const getToken = () => localStorage.getItem('portal_token') || ''
export const setToken = (t) => localStorage.setItem('portal_token', t)
export const clearToken = () => localStorage.removeItem('portal_token')

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// ============ Request Interceptor ============
// 在每个请求头上自动塞入 Authorization（除非调用方已经塞了，比如登录页不需要带）
service.interceptors.request.use((config) => {
  config.headers = config.headers || {}
  if (!config.headers.Authorization) {
    const token = getToken()
    if (token) config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ============ 同消息去重 ============
const DEDUP_WINDOW = 1500
let lastErrMsg = ''
let lastErrTime = 0
const showErrorOnce = (msg) => {
  const now = Date.now()
  if (msg === lastErrMsg && now - lastErrTime < DEDUP_WINDOW) return
  lastErrMsg = String(msg || '')
  lastErrTime = now
  ElMessage.error(lastErrMsg)
}

// ============ 401 单点闸门 ============
// 触发后除了 clearToken,还要主动清 Pinia store.userInfo,
// 否则头部 / 个人中心仍会展示一个"已过期但头像未刷新"的僵尸用户态。
const UNAUTH_WINDOW = 2000
let lastUnauthTime = 0
const triggerUnauth = () => {
  const now = Date.now()
  if (now - lastUnauthTime < UNAUTH_WINDOW) return
  lastUnauthTime = now
  // 401 直接用统一的"登录已失效"文案，不暴露后端的"缺少登录凭证"等内部术语
  showErrorOnce('登录已失效，请重新登录')
  // 清理 token
  try { clearToken() } catch (e) {}
  // 同步清理 Pinia store.userInfo / roles / permissions —— 必须走 store action
  // 因为我们在这里不能直接 import store (循环依赖风险),所以通过事件总线通知 app
  window.dispatchEvent(new CustomEvent('portal:auth-expired'))
}

service.interceptors.response.use(response => {
  const res = response.data
  if (res && res.code === 200) return res.data
  // 业务 401：登录态失效 —— 去重 + 单点处理
  if (res && Number(res.code) === 401) {
    if (!response.config?.silentError) triggerUnauth()
    return Promise.reject(new Error('登录已失效'))
  }
  // 其他业务错误：同消息去重
  if (!response.config?.silentError) {
    showErrorOnce(res?.message || '操作失败')
  }
  return Promise.reject(new Error(res?.message || '操作失败'))
}, error => {
  // 网络层错误：同消息去重
  if (!error.config?.silentError) {
    showErrorOnce(error.message || '网络异常')
  }
  return Promise.reject(error)
})

export default service

