import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 前台门户 axios 实例。
 *
 * 注意：portal 是"纯浏览"模式，request 默认不携带 Authorization；
 * store/user.js 仍可能持有 token（如有人后续接入登录流程），由调用方
 * 自行在请求配置里通过 headers.Authorization 注入。
 */

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

service.interceptors.response.use(response => {
  const res = response.data
  if (res && res.code === 200) return res.data
  if (!response.config || !response.config.silentError) {
    ElMessage.error(res?.message || '操作失败')
  }
  return Promise.reject(new Error(res?.message || '操作失败'))
}, error => {
  if (!error.config || !error.config.silentError) {
    ElMessage.error(error.message || '网络异常')
  }
  return Promise.reject(error)
})

export default service

// 保留旧 store/user.js / 外部调用方使用的 token 工具方法，导出但不强制使用
export const getToken = () => localStorage.getItem('portal_token') || ''
export const setToken = (t) => localStorage.setItem('portal_token', t)
export const clearToken = () => localStorage.removeItem('portal_token')
