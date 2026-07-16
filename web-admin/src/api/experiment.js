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
// 实验计划中出现的班级（去重）—— 实验评分下拉用
export const expClasses = () => request.get('/experiment/plan/classes')

// 实验分配
//   - assignPage：分页查询分配记录
//   - assignByClass：按班级批量分配，body = { planId, className }，返回新增条数
//   - assignOne：单个学生手动分配，body = { planId, studentId }
//   - assignComplete：标记完成 + 录成绩，body = { score, comment }
//   - assignRemove：批量撤销（软删），body = [id1, id2, ...]
export const assignPage     = (params) => request.get('/experiment/assignment/page', { params })
export const assignByClass  = (data)   => request.post('/experiment/assignment', data)
export const assignOne      = (data)   => request.post('/experiment/assignment/one', data)
export const assignComplete = (id, data) => request.put(`/experiment/assignment/${id}/complete`, data)
export const assignRemove   = (ids)    => request.delete('/experiment/assignment', { data: ids })
