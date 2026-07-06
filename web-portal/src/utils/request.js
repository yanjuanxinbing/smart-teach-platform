import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

service.interceptors.response.use(response => {
  const res = response.data
  if (res.code === 200) return res.data
  return Promise.reject(new Error(res.message || '操作失败'))
})

export default service
