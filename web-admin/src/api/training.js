import request from '@/utils/request'

// 实训计划
export const trainingPage = (params) => request.get('/training/plan/page', { params })
export const trainingDetail = (id) => request.get(`/training/plan/${id}`)
export const trainingAdd = (data) => request.post('/training/plan', data)
export const trainingEdit = (data) => request.put('/training/plan', data)
export const trainingRemove = (ids) => request.delete('/training/plan', { data: ids })
export const trainingPublish = (id) => request.put(`/training/plan/${id}/publish`)
export const trainingApprove = (id, remark) => request.put(`/training/plan/${id}/approve`, { remark })
export const trainingReject = (id, remark) => request.put(`/training/plan/${id}/reject`, { remark })
export const trainingFinish = (id) => request.put(`/training/plan/${id}/finish`)
export const trainingRevertDraft = (id) => request.put(`/training/plan/${id}/revert-draft`)
export const trainingClasses = () => request.get('/training/plan/classes')

// 实训报名
export const regPage = (params) => request.get('/training/registration/page', { params })
export const regAdd = (data) => request.post('/training/registration', data)
// 管理员代报名（直接已通过，无需审核）；区别于 regAdd —— 那个走 status=0 待审核
export const regAdminAdd = (data) => request.post('/training/registration/admin-add', data)
export const regReview = (id, data) => request.put(`/training/registration/${id}/review`, data)
export const regSignIn = (id) => request.put(`/training/registration/${id}/sign-in`)
export const regSignOut = (id) => request.put(`/training/registration/${id}/sign-out`)
export const regGrade = (id, data) => request.put(`/training/registration/${id}/grade`, data)
export const regRemove = (ids) => request.delete('/training/registration', { data: ids })
