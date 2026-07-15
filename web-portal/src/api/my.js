import request from '@/utils/request'

/* ============================================================================
 *  「我的学习中心」接口契约
 * ----------------------------------------------------------------------------
 *  GET /api/portal/my/courses
 *      Query : current, size, q, tag          （**不再带 studentId**）
 *      Auth  : 需登录,权限 course:my:list
 *      Resp  : { records: PortalMyCourseVO[], total, pageNum, pageSize }
 *
 *  GET /api/portal/my/assignments
 *      Query : current, size, status
 *              status ∈ { 'pending' | 'submitted' | 'graded' }
 *      Auth  : assignment:my:list
 *      字段  : assignmentId, courseId, courseName, title, deadline,
 *              status, score, submittedAt, gradedAt
 *
 *  GET /api/portal/my/trainings
 *      Query : current, size, status
 *              status ∈ { 'not_started' | 'in_progress' | 'done' }
 *      Auth  : training:my:list
 *      字段  : registrationId, trainingId, planName, projectName, semester,
 *              className, teacherName, startDate, endDate, status, progress
 *
 *  【为什么 URL 不带 studentId】
 *    1. 后端从 JWT 还原身份:JwtAuthenticationFilter 已经把
 *       UserContext.setUserId(jwtUtil.getUserId(token)),所以后端只用
 *       UserContext.getUserId() 一行就能拿到"真实的"学生 ID。
 *    2. URL 里出现 studentId 会给调用方一种"改它就能越权"的错觉 —— 即便后端
 *       不信任请求参数,前端也要明确不传,把反 IDOR 写在 HTTP 层。
 *    3. Pinia store 的 userInfo.userId 仅在前端路由守卫 / 自身 UI (头像 / 姓名)
 *       展示用;**绝不能**作为后端过滤依据。
 *
 *  【前端如何保证"当前用户信息已被识别"】
 *    - 登录成功后 web-portal/src/store/user.js 的 loginAction 会立即把
 *      LoginVO 的 {userId, username, realName, avatar} 写入 store.userInfo
 *      并持久化到 localStorage.portal_user_info;
 *    - 页面刷新时,store 创建时同步从 localStorage 恢复 userInfo;
 *    - 若 localStorage 因清理/多端登录不一致导致 userInfo 缺失,
 *      由 MyLearningLayout 的 onMounted 兜底调用 fetchUserInfo()。
 *
 *  【水平越权（服务端）参考】
 *      Long studentId = UserContext.getUserId();
 *      if (studentId == null) throw new BusinessException(UNAUTHORIZED);
 *      wrapper.eq(<entity>::getStudentId, studentId);
 *  已在后端 PortalMyLearningServiceImpl.requireStudent() 实现。
 * ========================================================================== */

// 三个 my-* 接口的统一静音配置(后端就绪后移除)
const SILENT = { silentError: true }

// 直接透传过滤 / 分页参数 —— 不再注入 studentId
export const myCourses     = (params = {}) =>
  request.get('/portal/my/courses',     { params, ...SILENT })

export const myAssignments = (params = {}) =>
  request.get('/portal/my/assignments', { params, ...SILENT })

export const myTrainings   = (params = {}) =>
  request.get('/portal/my/trainings',   { params, ...SILENT })
