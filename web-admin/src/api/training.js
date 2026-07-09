import request from '@/utils/request'

// 实训计划
export const trainingPage = (params) => request.get('/training/plan/page', { params })
export const trainingDetail = (id) => request.get(`/training/plan/${id}`)
export const trainingAdd = (data) => request.post('/training/plan', data)
export const trainingEdit = (data) => request.put('/training/plan', data)
export const trainingRemove = (ids) => request.delete('/training/plan', { data: ids })
export const trainingPublish = (id) => request.put(`/training/plan/${id}/publish`)
export const trainingSubmitReview = (id) => request.put(`/training/plan/${id}/submit-review`)
export const trainingApprove = (id, remark) => request.put(`/training/plan/${id}/approve`, { remark })
export const trainingReject = (id, remark) => request.put(`/training/plan/${id}/reject`, { remark })
export const trainingFinish = (id) => request.put(`/training/plan/${id}/finish`)
export const trainingRevertDraft = (id) => request.put(`/training/plan/${id}/revert-draft`)

// 实训报名
export const regPage = (planId, params) => request.get('/training/registration/page', { params: { planId, ...params } })
export const regAdd = (data) => request.post('/training/registration', data)
export const regReview = (id, data) => request.put(`/training/registration/${id}/review`, data)
export const regGrade = (id, data) => request.put(`/training/registration/${id}/grade`, data)
export const regRemove = (ids) => request.delete('/training/registration', { data: ids })

// 实训成绩
export const scoreList = (registrationId) => request.get('/training/score/list', { params: { registrationId } })
export const scoreSave = (registrationId, data) => request.post(`/training/score/${registrationId}`, data)
export const regGradeItems = (id, data) => request.put(`/training/registration/${id}/grade-items`, data)
