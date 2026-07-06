import request from '@/utils/request'

// 门户内容管理
export const portalPage = (params) => request.get('/portal/manage/page', { params })
export const portalAdd = (data) => request.post('/portal/manage', data)
export const portalEdit = (data) => request.put('/portal/manage', data)
export const portalRemove = (ids) => request.delete('/portal/manage', { data: ids })
export const portalPublish = (id) => request.put(`/portal/manage/${id}/publish`)
export const portalOffline = (id) => request.put(`/portal/manage/${id}/offline`)

// 门户前台（公开）
export const portalList = (type, limit) => request.get('/portal/site/list', { params: { type, limit } })
export const portalDetail = (id) => request.get(`/portal/site/${id}`)
