import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)
// 退出登录：即便 token 已过期，也不需要弹错误 toast（前端本来就要清状态）
export const logout = () => request.post('/auth/logout', null, { silentError: true })
export const getUserInfo = () => request.get('/auth/me')
export const register = (data) => request.post('/auth/register', data)
