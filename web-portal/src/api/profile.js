import request from '@/utils/request'

export const updateMyProfile = (data) => request.put('/profile/me', data)
export const changeOwnPassword = (data) => request.post('/profile/me/password', data)
export const myOperationLog = (params) => request.get('/profile/me/logs', { params })

// 消息中心
export const myMessages = (params) => request.get('/message/page', { params })
export const markRead = (ids) => request.post('/message/read', { ids })
export const markAllRead = () => request.post('/message/read-all')
export const unreadCount = () => request.get('/message/unread-count')
