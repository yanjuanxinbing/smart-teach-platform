import request from '@/utils/request'

/* ============================================================================
 *  「我的实验」门户侧接口契约
 * ----------------------------------------------------------------------------
 *  GET /api/portal/my/experiments
 *      Query : current, size, q, status
 *              status ∈ { 'not_started' | 'in_progress' | 'done' }
 *      Auth  : 需登录,权限 experiment:my:list
 *              （studentId 由后端从 JWT 取 UserContext.getUserId()，前端不传，反 IDOR）
 *      Resp  : { records: PortalMyExperimentVO[], total, pageNum, pageSize }
 *
 *      PortalMyExperimentVO 字段：
 *        experimentId   实验（明细）ID    —— experiment_plan_item.id
 *        planId         所属实验计划ID    —— experiment_plan.id
 *        experimentName 实验名称
 *        courseId       所属课程ID
 *        courseName     所属课程名
 *        semester       学期
 *        className      班级
 *        teacherName    指导教师
 *        labRoom        实验室
 *        startDate      开始时间
 *        endDate        结束时间
 *        status         状态枚举 —— not_started / in_progress / done
 *
 *  详情：GET /api/portal/my/experiments/{id}
 *      返回 PortalExperimentDetailVO（含 purpose/content/requirement + 所属 plan 上下文）
 *
 *  【实验 vs 实训的关键差异】
 *      - 学生不能自报名;门户不提供 available/register 接口
 *      - 学生只看到自己被分配的实验,没有分配的看不到
 *      - 状态枚举精简:仅 1已分配 / 3已完成,无 pending_review
 *      - 实验明细状态由 classDate 与 today 推算
 * ========================================================================== */

const SILENT = { silentError: true }

export const myExperiments = (params = {}) =>
  request.get('/portal/my/experiments', { params, ...SILENT })

export const myExperimentDetail = (id) =>
  request.get(`/portal/my/experiments/${id}`, { ...SILENT })