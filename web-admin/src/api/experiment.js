import request from '@/utils/request'

// 实验计划
export const expPage = (params) => request.get('/experiment/plan/page', { params })
export const expDetail = (id) => request.get(`/experiment/plan/${id}`)
export const expAdd = (data) => request.post('/experiment/plan', data)
export const expEdit = (data) => request.put('/experiment/plan', data)
export const expRemove = (ids) => request.delete('/experiment/plan', { data: ids })
export const expSubmit = (id) => request.put(`/experiment/plan/${id}/submit`)
export const expApprove = (id, remark) => request.put(`/experiment/plan/${id}/approve`, { remark })
export const expReject = (id, remark) => request.put(`/experiment/plan/${id}/reject`, { remark })
