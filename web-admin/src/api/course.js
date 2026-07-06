import request from '@/utils/request'

// 课程
export const coursePage = (params) => request.get('/course/manage/page', { params })
export const courseDetail = (id) => request.get(`/course/manage/${id}`)
export const courseAdd = (data) => request.post('/course/manage', data)
export const courseEdit = (data) => request.put('/course/manage', data)
export const courseRemove = (ids) => request.delete('/course/manage', { data: ids })
export const courseChangeStatus = (id, status) => request.put(`/course/manage/${id}/status`, null, { params: { status } })
export const myCourses = () => request.get('/course/manage/my')

// 章节
export const chapterList = (courseId) => request.get('/course/chapter/list', { params: { courseId } })
export const chapterAdd = (data) => request.post('/course/chapter', data)
export const chapterEdit = (data) => request.put('/course/chapter', data)
export const chapterRemove = (id) => request.delete(`/course/chapter/${id}`)

// 课程内容
export const contentByChapter = (chapterId) => request.get('/course/content/list', { params: { chapterId } })
export const contentByCourse = (courseId) => request.get('/course/content/by-course', { params: { courseId } })
export const contentAdd = (data) => request.post('/course/content', data)
export const contentEdit = (data) => request.put('/course/content', data)
export const contentRemove = (ids) => request.delete('/course/content', { data: ids })

// 教学计划
export const planPage = (params) => request.get('/course/plan/page', { params })
export const planDetail = (id) => request.get(`/course/plan/${id}`)
export const planAdd = (data) => request.post('/course/plan', data)
export const planEdit = (data) => request.put('/course/plan', data)
export const planRemove = (ids) => request.delete('/course/plan', { data: ids })
export const planSubmit = (id) => request.put(`/course/plan/${id}/submit`)
export const planApprove = (id, remark) => request.put(`/course/plan/${id}/approve`, { remark })
export const planReject = (id, remark) => request.put(`/course/plan/${id}/reject`, { remark })
