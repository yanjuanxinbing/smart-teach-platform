import request from '@/utils/request'

// 公开课程中心（前台门户）
export const listCourses = (params) => request.get('/portal/course/page', { params })
export const courseDetail = (id) => request.get(`/portal/course/${id}`)
export const courseChapters = (id) => request.get(`/portal/course/${id}/chapters`)
export const listTeachers = () => request.get('/portal/teacher/list')

// 学生加入课程 —— 与 my.js 的 myCourses / course_enrollment 表配套
// 后端契约：
//   POST /api/portal/my/courses/add
//     Body: { courseId: number }
//     Resp: { success: true } 或 PortalMyCourseVO
//   反 IDOR：studentId 由后端从 JWT 取 UserContext.getUserId()，前端不传
export const joinCourse = (courseId) =>
  request.post('/portal/my/courses/add', { courseId }, { silentError: true })
