import request from '@/utils/request'

// 授课管理（课程-教师关系）
export const teachPage = (params) => request.get('/course/teacher/page', { params })
export const teachAssign = (data) => request.post('/course/teacher', data)
export const teachChangeStatus = (id, status) =>
  request.put(`/course/teacher/${id}/status`, null, { params: { status } })
export const teachRemove = (ids) => request.delete('/course/teacher', { data: ids })

// 下拉参考数据：课程-教师关系
export const listTeachersByCourse = (courseId) =>
  request.get(`/course/teacher/teachers-by-course/${courseId}`)
export const listCoursesByTeacher = (teacherId) =>
  request.get(`/course/teacher/courses-by-teacher/${teacherId}`)

// 用户侧：按角色编码列出所有启用用户（给教师选择器用）
export const userListByRole = (roleCode) =>
  request.get('/system/user/list-by-role', { params: { roleCode } })