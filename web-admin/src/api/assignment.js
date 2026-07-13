import request from '@/utils/request'

// ===== 作业（教师发布/编辑/删除/发布/关闭） =====
// assignDetail 默认静默（不弹全局 toast），调用方根据 2001 自定义提示文案（"作业已删除" 等）
export const assignPage = (params) => request.get('/assignment/page', { params })
export const assignDetail = (id) => request.get(`/assignment/${id}`, { silentError: true })
export const assignAdd = (data) => request.post('/assignment', data)
export const assignEdit = (data) => request.put('/assignment', data)
export const assignRemove = (ids) => request.delete('/assignment', { data: ids })
export const assignPublish = (id) => request.put(`/assignment/${id}/publish`)
export const assignClose = (id) => request.put(`/assignment/${id}/close`)

// ===== 作业提交（教师批改 + 学生提交） =====
// 教师批改端
export const submissionPage = (params) => request.get('/assignment/submission/page', { params })
// 学生本人
export const submissionMyPage = (params) => request.get('/assignment/submission/my-page', { params })
export const submissionLatest = (assignmentId) => request.get('/assignment/submission/latest', { params: { assignmentId } })
// 草稿 / 提交
export const submissionSaveDraft = (data) => request.post('/assignment/submission/save-draft', data)
export const submissionSubmit = (data) => request.post('/assignment/submission/submit', data)
// 批改打分（静默：避免 2001 时弹全局 toast，由调用方刷新列表后自提示）
export const submissionGrade = (id, data) => request.put(`/assignment/submission/${id}/grade`, data, { silentError: true })
// 删除
export const submissionRemove = (ids) => request.delete('/assignment/submission', { data: ids })
