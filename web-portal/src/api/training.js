import request from '@/utils/request'

/* ============================================================================
 *  「实训计划浏览/报名」门户侧接口契约
 * ----------------------------------------------------------------------------
 *  GET  /api/portal/training/available
 *      Auth  : 需登录(任意角色)
 *      Resp  : PortalTrainingVO[]
 *      说明  : 列出所有 status=3(进行中) 的实训计划;
 *              每条记录带 registered / registrationStatus / registrationId,
 *              前端据此决定是否渲染「报名」按钮。
 *
 *  GET  /api/portal/training/{id}
 *      Auth  : 需登录(任意角色)
 *      Resp  : PortalTrainingVO(包含 objective/content/assessment 等详情字段)
 *
 *  POST /api/portal/training/register
 *      Body  : { planId: number }
 *      Auth  : 需登录,studentId 严格从 JWT 取(反 IDOR,请求体不接受 studentId)
 *      Resp  : PortalTrainingVO(registered=true,registrationStatus=0 待审核)
 *
 *  PortalTrainingVO 字段:
 *      planId, planTitle, projectName, semester, className, teacherName,
 *      location, startDate, endDate, status, capacity, registeredCount,
 *      objective, content, assessment,
 *      registered(Boolean), registrationId(Long?), registrationStatus(0/1/2/3?),
 *      // === 学生成绩(来自 training_registration,仅当 registrationStatus=3 时有值)===
 *      score(BigDecimal?), regularScore, examScore,
 *      regularWeight(Int 0-100), examWeight(Int 0-100),
 *      comment(String), gradedAt(LocalDateTime?),
 *      scoreAvailable(Boolean: 任一分数字段或评语非空即 true)
 *
 *  「我的实训」端点(/api/portal/my/trainings)继续走 my.js,
 *   待审核数据通过 status=0 → vo.status='pending_review' 返回。
 * ========================================================================== */

export const availableTrainings = () =>
  request.get('/portal/training/available', { silentError: true })

export const trainingDetail = (id) =>
  request.get(`/portal/training/${id}`, { silentError: true })

export const registerTraining = (planId) =>
  request.post('/portal/training/register', { planId }, { silentError: true })