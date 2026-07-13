import request from '@/utils/request'

export const listByType = (type, limit) => request.get('/portal/site/list', { params: { type, limit } })
export const detail = (id) => request.get(`/portal/site/${id}`)
export const siteStats = () => request.get('/portal/site/stats')