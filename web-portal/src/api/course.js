import request from '@/utils/request'

// 公开课程中心（前台门户）
export const listCourses = (params) => request.get('/portal/course/page', { params })
export const courseDetail = (id) => request.get(`/portal/course/${id}`)
export const courseChapters = (id) => request.get(`/portal/course/${id}/chapters`)
export const listTeachers = () => request.get('/portal/teacher/list')
