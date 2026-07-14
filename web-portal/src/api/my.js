import request from '@/utils/request'
import { useUserStore } from '@/store/user'

/* ============================================================================
 *  「我的学习中心」后端契约草案 —— 供后端同事对接实现
 * ----------------------------------------------------------------------------
 *  GET /api/portal/my/courses
 *      Query : studentId (仅自描述;**禁止以此为准进行鉴权**),
 *              current, size, q, tag
 *      Auth  : 需登录,权限 course:my:list (约定: xxx:my:list / :my:query)
 *      Resp  : { records: Course[], total, current, size }
 *      字段  : { id, courseName, courseCode, teacherName, courseType,
 *               totalHours, cover, progress (0-100), enrolledAt }
 *
 *  GET /api/portal/my/assignments
 *      Query : studentId, current, size, status
 *              status ∈ { 'pending' | 'submitted' | 'graded' }
 *      Auth  : assignment:my:list
 *      字段  : { id, courseName, title, dueAt (ISO), status,
 *               score (仅 graded), submittedAt, gradedAt }
 *
 *  GET /api/portal/my/trainings
 *      Query : studentId, current, size, progress
 *              progress ∈ { 'not_started' | 'in_progress' | 'done' }
 *      Auth  : training:my:list
 *      字段  : { id, planName, title, stage, startAt, endAt,
 *               progress (0-100), progressText }
 *
 *  【安全 - 水平越权防护】
 *    请求里带的 studentId 仅用于自描述/审计,**严禁** 把它当作鉴权来源。
 *    服务端必须:
 *      Long studentId = UserContext.getUserId();
 *      if (studentId == null) throw new BusinessException(UNAUTHORIZED);
 *      wrapper.eq(<entity>::getStudentId, studentId);
 *    参考实现:src/main/java/com/smartteach/modules/course/assignment/
 *              service/impl/AssignmentSubmissionServiceImpl.java:64-70
 *
 *  【迭代 1 说明】
 *    上述三个端点暂未实现。本轮前端先打通路由、视图与 API 调用层,
 *    用 silentError 抑制 axios 的 4xx toast,页面降级到「功能开发中」友好空态。
 *    后端实现完成后,**移除下方 SILENT 常量的 spread 即可恢复真实错误提示**。
 * ========================================================================== */

// 三个 my-* 接口的统一静音配置(后端就绪后移除)
const SILENT = { silentError: true }

// 把当前登录学生的 userId 自动注入到请求参数中.
// 服务端不应信任这个值(应从 UserContext 再校验一次),这里仅作为自描述.
const withStudent = (extra = {}) => {
  const userStore = useUserStore()
  const uid = Number(userStore.userInfo?.userId)
  return { ...extra, studentId: Number.isFinite(uid) && uid > 0 ? uid : -1 }
}

export const myCourses     = (params = {}) =>
  request.get('/portal/my/courses',     { params: withStudent(params), ...SILENT })

export const myAssignments = (params = {}) =>
  request.get('/portal/my/assignments', { params: withStudent(params), ...SILENT })

export const myTrainings   = (params = {}) =>
  request.get('/portal/my/trainings',   { params: withStudent(params), ...SILENT })
