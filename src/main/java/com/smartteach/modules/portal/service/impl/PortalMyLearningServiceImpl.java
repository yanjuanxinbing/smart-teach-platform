package com.smartteach.modules.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartteach.common.enums.ExperimentStatus;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.course.assignment.entity.Assignment;
import com.smartteach.modules.course.assignment.entity.AssignmentSubmission;
import com.smartteach.modules.course.assignment.mapper.AssignmentMapper;
import com.smartteach.modules.course.assignment.mapper.AssignmentSubmissionMapper;
import com.smartteach.modules.course.entity.Course;
import com.smartteach.modules.course.mapper.CourseMapper;
import com.smartteach.modules.experiment.entity.ExperimentAssignment;
import com.smartteach.modules.experiment.entity.ExperimentPlan;
import com.smartteach.modules.experiment.entity.ExperimentPlanItem;
import com.smartteach.modules.experiment.mapper.ExperimentAssignmentMapper;
import com.smartteach.modules.experiment.mapper.ExperimentPlanItemMapper;
import com.smartteach.modules.experiment.mapper.ExperimentPlanMapper;
import com.smartteach.modules.portal.entity.CourseEnrollment;
import com.smartteach.modules.portal.mapper.CourseEnrollmentMapper;
import com.smartteach.modules.portal.service.PortalMyLearningService;
import com.smartteach.modules.portal.vo.PortalExperimentDetailVO;
import com.smartteach.modules.portal.vo.PortalMyAssignmentVO;
import com.smartteach.modules.portal.vo.PortalMyCourseVO;
import com.smartteach.modules.portal.vo.PortalMyExperimentVO;
import com.smartteach.modules.portal.vo.PortalMyTrainingVO;
import com.smartteach.modules.portal.vo.PortalTrainingVO;
import com.smartteach.modules.system.entity.SysAssignmentClass;
import com.smartteach.modules.system.entity.SysUserClass;
import com.smartteach.modules.system.mapper.SysAssignmentClassMapper;
import com.smartteach.modules.system.mapper.SysUserClassMapper;
import com.smartteach.modules.training.dto.TrainingRegistrationSaveDTO;
import com.smartteach.modules.training.entity.TrainingPlan;
import com.smartteach.modules.training.entity.TrainingRegistration;
import com.smartteach.modules.training.mapper.TrainingPlanMapper;
import com.smartteach.modules.training.mapper.TrainingRegistrationMapper;
import com.smartteach.modules.training.service.TrainingRegistrationService;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 「我的学习中心」门户侧服务 —— 三个端点共用一份"student_id 来自 UserContext"的反 IDOR 实现
 *
 * <p>反 IDOR 关键：所有 SQL 都不读 studentId 请求参数；只信任调用方传入的 studentId
 * （必须由 Controller 从 UserContext.getUserId() 取）。</p>
 */
@Service
@RequiredArgsConstructor
public class PortalMyLearningServiceImpl implements PortalMyLearningService {

    private static final Map<Integer, String> COURSE_TYPE_LABEL = new HashMap<>();
    static {
        COURSE_TYPE_LABEL.put(1, "必修");
        COURSE_TYPE_LABEL.put(2, "选修");
        COURSE_TYPE_LABEL.put(3, "通识");
    }

    private final CourseEnrollmentMapper enrollmentMapper;
    private final AssignmentMapper assignmentMapper;
    private final AssignmentSubmissionMapper submissionMapper;
    private final TrainingRegistrationMapper trainingRegistrationMapper;
    private final TrainingPlanMapper trainingPlanMapper;
    private final CourseMapper courseMapper;
    private final TrainingRegistrationService trainingRegistrationService;
    private final SysUserMapper sysUserMapper;
    private final ExperimentAssignmentMapper experimentAssignmentMapper;
    private final ExperimentPlanMapper experimentPlanMapper;
    private final SysUserClassMapper sysUserClassMapper;
    private final SysAssignmentClassMapper sysAssignmentClassMapper;
    private final ExperimentPlanItemMapper experimentPlanItemMapper;

    @Override
    public IPage<PortalMyCourseVO> myCourses(Long studentId, long pageNum, long pageSize, String keyword) {
        requireStudent(studentId);
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getStudentId, studentId)
                .eq(CourseEnrollment::getStatus, 1)        // 进行中
                .orderByDesc(CourseEnrollment::getEnrolledAt);
        if (StringUtils.isNotBlank(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(CourseEnrollment::getCourseName, kw)
                    .or().like(CourseEnrollment::getCourseCode, kw)
                    .or().like(CourseEnrollment::getTeacherName, kw));
        }
        IPage<CourseEnrollment> page = enrollmentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(this::toCourseVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalMyCourseVO joinCourse(Long studentId, Long courseId) {
        requireStudent(studentId);
        if (courseId == null) {
            throw new BusinessException("课程ID不能为空");
        }

        // 1) 校验课程存在且已发布
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException("课程尚未发布,无法加入");
        }

        // 2) 幂等:同一学生同一课程若已选(状态 1=进行中),直接返回现有记录
        LambdaQueryWrapper<CourseEnrollment> existWrap = new LambdaQueryWrapper<>();
        existWrap.eq(CourseEnrollment::getStudentId, studentId)
                .eq(CourseEnrollment::getCourseId, courseId)
                .eq(CourseEnrollment::getStatus, 1);
        CourseEnrollment existing = enrollmentMapper.selectOne(existWrap);
        if (existing != null) {
            return toCourseVO(existing);
        }

        // 3) 落 course_enrollment 表(冗余字段填充,避免列表 N+1)
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setCourseName(course.getCourseName());
        enrollment.setCourseCode(course.getCourseCode());
        enrollment.setTeacherName(course.getTeacherName());
        // coverImage 当前 Course 实体未带,从 course.cover_image 列读;若该列尚未在实体上,
        // 后续 PortalMyCourseVO.coverImage 仍然为空字符串,UI 端有兜底渐变色,不影响主流程
        enrollment.setTotalHours(course.getTotalHours());
        enrollment.setCourseType(course.getCourseType());
        enrollment.setProgress(0);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(1);   // 进行中

        enrollmentMapper.insert(enrollment);
        return toCourseVO(enrollment);
    }

    @Override
    public IPage<PortalMyAssignmentVO> myAssignments(Long studentId, long pageNum, long pageSize, String status) {
        requireStudent(studentId);

        // 1) 学生所属班级
        List<SysUserClass> userClasses = sysUserClassMapper.selectList(
                new LambdaQueryWrapper<SysUserClass>().eq(SysUserClass::getUserId, studentId));
        if (userClasses.isEmpty()) {
            // 学生未挂任何班级 → 看不到任何作业
            return new Page<>(pageNum, pageSize, 0);
        }
        List<Long> classIds = userClasses.stream()
                .map(SysUserClass::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2) 班级关联的作业 ID
        List<SysAssignmentClass> acList = sysAssignmentClassMapper.selectList(
                new LambdaQueryWrapper<SysAssignmentClass>().in(SysAssignmentClass::getClassId, classIds));
        Set<Long> visibleAssignmentIds = acList.stream()
                .map(SysAssignmentClass::getAssignmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (visibleAssignmentIds.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        // 3) 拉取已发布/已截止的作业(按截止时间倒序)
        List<Assignment> assignments = assignmentMapper.selectList(
                new LambdaQueryWrapper<Assignment>()
                        .in(Assignment::getId, visibleAssignmentIds)
                        .in(Assignment::getStatus, 1, 2)
                        .orderByDesc(Assignment::getDeadline));

        // 4) 该学生所有提交，按 assignment_id 取最新一条
        LambdaQueryWrapper<AssignmentSubmission> submissionWrap = new LambdaQueryWrapper<>();
        submissionWrap.eq(AssignmentSubmission::getStudentId, studentId);
        List<AssignmentSubmission> submissions = submissionMapper.selectList(submissionWrap);

        Map<Long, AssignmentSubmission> latestByAssignment = new HashMap<>();
        for (AssignmentSubmission s : submissions) {
            AssignmentSubmission prev = latestByAssignment.get(s.getAssignmentId());
            if (prev == null || isMoreAdvanced(s, prev)) {
                latestByAssignment.put(s.getAssignmentId(), s);
            }
        }

        // 5) 组装并按 status 过滤
        List<PortalMyAssignmentVO> all = new ArrayList<>(assignments.size());
        for (Assignment a : assignments) {
            AssignmentSubmission latest = latestByAssignment.get(a.getId());
            PortalMyAssignmentVO vo = toAssignmentVO(a, latest);
            if (matchesStatus(vo.getStatus(), status)) all.add(vo);
        }

        // 6) 内存分页
        long total = all.size();
        long from = Math.max(0L, (pageNum - 1) * pageSize);
        long to = Math.min(total, from + pageSize);
        List<PortalMyAssignmentVO> slice = from >= total ? Collections.emptyList() : new ArrayList<>(all.subList((int) from, (int) to));

        Page<PortalMyAssignmentVO> result = new Page<>(pageNum, pageSize, total);
        result.setRecords(slice);
        return result;
    }

    @Override
    public IPage<PortalMyTrainingVO> myTrainings(Long studentId, long pageNum, long pageSize, String status) {
        requireStudent(studentId);

        // 1) 学生报名记录
        //   包含 status=0(待审核):学生门户自报名后,等待管理员审核期间对学生可见;
        //   不包含 status=2(已驳回):驳回即"未通过",学生门户不应再展示。
        LambdaQueryWrapper<TrainingRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRegistration::getStudentId, studentId)
                .in(TrainingRegistration::getStatus, 0, 1, 3)
                .orderByDesc(TrainingRegistration::getCreateTime);
        IPage<TrainingRegistration> regPage = trainingRegistrationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 2) 批量回查 training_plan 详情,避免逐个回查
        Set<Long> planIds = new HashSet<>();
        for (TrainingRegistration r : regPage.getRecords()) planIds.add(r.getPlanId());
        Map<Long, TrainingPlan> planMap = planIds.isEmpty()
                ? Collections.emptyMap()
                : trainingPlanMapper.selectBatchIds(planIds).stream().collect(Collectors.toMap(TrainingPlan::getId, p -> p));

        LocalDate today = LocalDate.now();
        List<PortalMyTrainingVO> filtered = new ArrayList<>();
        for (TrainingRegistration r : regPage.getRecords()) {
            TrainingPlan plan = planMap.get(r.getPlanId());
            PortalMyTrainingVO vo = toTrainingVO(r, plan, today);
            if (matchesTrainingStatus(vo.getStatus(), status)) filtered.add(vo);
        }
        Page<PortalMyTrainingVO> voPage = new Page<>(regPage.getCurrent(), regPage.getSize(), regPage.getTotal());
        voPage.setRecords(filtered);
        return voPage;
    }

    // ====================================================================
    //  门户-实训计划浏览与自报名
    // ====================================================================

    @Override
    public List<PortalTrainingVO> listAvailableTrainings(Long studentId) {
        requireStudent(studentId);
        // 仅展示 status=3(进行中)的计划 —— 与 TrainingRegistrationServiceImpl.register() 的可报名校验保持一致
        LambdaQueryWrapper<TrainingPlan> planWrap = new LambdaQueryWrapper<>();
        planWrap.eq(TrainingPlan::getStatus, 3)
                .orderByDesc(TrainingPlan::getCreateTime);
        List<TrainingPlan> plans = trainingPlanMapper.selectList(planWrap);
        if (plans.isEmpty()) return Collections.emptyList();

        return plans.stream().map(p -> toPortalTrainingVO(p, studentId)).collect(Collectors.toList());
    }

    @Override
    public PortalTrainingVO getTrainingDetail(Long studentId, Long planId) {
        requireStudent(studentId);
        if (planId == null) throw new BusinessException("实训计划ID不能为空");
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        return toPortalTrainingVO(plan, studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalTrainingVO registerForTraining(Long studentId, Long planId) {
        requireStudent(studentId);
        if (planId == null) throw new BusinessException("实训计划ID不能为空");

        // 1) 计划存在性 + 状态校验（service 内也会校验，这里提前报错更友好）
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        if (plan.getStatus() == null || plan.getStatus() != 3) {
            throw new BusinessException("该实训计划当前不可报名");
        }

        // 2) 重复报名（同学生同计划任意状态都算已报名，避免反复点）
        long dup = trainingRegistrationMapper.selectCount(new LambdaQueryWrapper<TrainingRegistration>()
                .eq(TrainingRegistration::getPlanId, planId)
                .eq(TrainingRegistration::getStudentId, studentId));
        if (dup > 0) {
            throw new BusinessException("你已报名该实训计划，无需重复报名");
        }

        // 3) 学生信息从 sys_user 拿 —— realName/phone
        SysUser user = sysUserMapper.selectById(studentId);
        if (user == null) throw new BusinessException(ResultCode.DATA_NOT_EXIST);

        // 4) 组装 DTO 并调用既有 register() 落 status=0(待审核)
        //    className 取自 plan —— 计划本就限定了班级
        TrainingRegistrationSaveDTO dto = new TrainingRegistrationSaveDTO();
        dto.setPlanId(planId);
        dto.setStudentId(studentId);
        dto.setStudentName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setClassName(plan.getClassName());
        // status 保持 null：register() 内部对 null 兜底为 0(待审核)
        trainingRegistrationService.register(dto);

        // 5) 重新读取并返回（带上冗余 planTitle）
        TrainingRegistration saved = trainingRegistrationMapper.selectOne(new LambdaQueryWrapper<TrainingRegistration>()
                .eq(TrainingRegistration::getPlanId, planId)
                .eq(TrainingRegistration::getStudentId, studentId)
                .orderByDesc(TrainingRegistration::getCreateTime)
                .last("LIMIT 1"));
        return toPortalTrainingVO(plan, studentId, saved);
    }

    /**
     * 把 TrainingPlan + 当前学生报名上下文拼装成 PortalTrainingVO。
     * 报名记录可空（学生未报名时）；为空时 registered=false,registrationId/Status=null。
     */
    private PortalTrainingVO toPortalTrainingVO(TrainingPlan plan, Long studentId) {
        TrainingRegistration reg = trainingRegistrationMapper.selectOne(new LambdaQueryWrapper<TrainingRegistration>()
                .eq(TrainingRegistration::getPlanId, plan.getId())
                .eq(TrainingRegistration::getStudentId, studentId)
                .orderByDesc(TrainingRegistration::getCreateTime)
                .last("LIMIT 1"));
        return toPortalTrainingVO(plan, studentId, reg);
    }

    private PortalTrainingVO toPortalTrainingVO(TrainingPlan plan, Long studentId, TrainingRegistration reg) {
        PortalTrainingVO vo = new PortalTrainingVO();
        vo.setPlanId(plan.getId());
        vo.setPlanTitle(plan.getPlanTitle());
        vo.setProjectName(plan.getProjectName());
        vo.setSemester(plan.getSemester());
        vo.setClassName(plan.getClassName());
        vo.setTeacherName(plan.getTeacherName());
        vo.setLocation(plan.getLocation());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setStatus(plan.getStatus());
        vo.setCapacity(plan.getCapacity());
        vo.setObjective(plan.getObjective());
        vo.setContent(plan.getContent());
        vo.setAssessment(plan.getAssessment());

        // 已报名人数（含待审核/已通过/已驳回，不含已删除），用于前端余位展示
        Long count = trainingRegistrationMapper.selectCount(new LambdaQueryWrapper<TrainingRegistration>()
                .eq(TrainingRegistration::getPlanId, plan.getId())
                .in(TrainingRegistration::getStatus, 0, 1, 2));
        vo.setRegisteredCount(count == null ? 0 : count.intValue());

        if (reg != null) {
            vo.setRegistered(true);
            vo.setRegistrationId(reg.getId());
            vo.setRegistrationStatus(reg.getStatus());
            // === 学生成绩（来自 training_registration）===
            // 仅当教师已登记成绩/评语时才填充；前端用 scoreAvailable 渲染"暂无成绩"提示
            vo.setScore(reg.getScore());
            vo.setRegularScore(reg.getRegularScore());
            vo.setExamScore(reg.getExamScore());
            vo.setRegularWeight(reg.getRegularWeight());
            vo.setExamWeight(reg.getExamWeight());
            vo.setComment(reg.getComment());
            vo.setGradedAt(reg.getUpdateTime());
            boolean hasScore = reg.getScore() != null
                    || reg.getRegularScore() != null
                    || reg.getExamScore() != null;
            boolean hasComment = reg.getComment() != null && !reg.getComment().isBlank();
            vo.setScoreAvailable(hasScore || hasComment);
        } else {
            vo.setRegistered(false);
        }
        return vo;
    }

    // ====================================================================
    //  门户-实验（学生只读 —— 仅看到自己被分配的实验）
    // ====================================================================

    @Override
    public IPage<PortalMyExperimentVO> myExperiments(Long studentId, long pageNum, long pageSize, String status, String keyword) {
        requireStudent(studentId);

        // 1) 查当前学生所有有效 assignment（status IN 1,3）
        LambdaQueryWrapper<ExperimentAssignment> regWrap = new LambdaQueryWrapper<>();
        regWrap.eq(ExperimentAssignment::getStudentId, studentId)
                .in(ExperimentAssignment::getStatus, 1, 3)
                .orderByDesc(ExperimentAssignment::getCreateTime);
        List<ExperimentAssignment> regs = experimentAssignmentMapper.selectList(regWrap);
        if (regs.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        // 2) 批量取 plan + items
        Set<Long> planIds = regs.stream().map(ExperimentAssignment::getPlanId).collect(Collectors.toSet());
        Map<Long, ExperimentPlan> planMap = experimentPlanMapper.selectBatchIds(planIds).stream()
                .collect(Collectors.toMap(ExperimentPlan::getId, p -> p));

        LambdaQueryWrapper<ExperimentPlanItem> itemWrap = new LambdaQueryWrapper<>();
        itemWrap.in(ExperimentPlanItem::getPlanId, planIds)
                // 显式组装 List<SFunction>,避免 SFunction<T,?>... varargs 的"创建泛型数组"堆污染警告
                .orderByAsc(Arrays.asList(ExperimentPlanItem::getPlanId, ExperimentPlanItem::getExpNo));
        List<ExperimentPlanItem> items = experimentPlanItemMapper.selectList(itemWrap);
        Map<Long, List<ExperimentPlanItem>> itemsByPlan = items.stream()
                .collect(Collectors.groupingBy(ExperimentPlanItem::getPlanId));

        // 3) 展开为 per-item VO，按 classDate 推算 status
        LocalDate today = LocalDate.now();
        List<PortalMyExperimentVO> all = new ArrayList<>();
        for (ExperimentAssignment reg : regs) {
            ExperimentPlan plan = planMap.get(reg.getPlanId());
            List<ExperimentPlanItem> planItems = itemsByPlan.get(reg.getPlanId());
            if (planItems == null) continue;
            for (ExperimentPlanItem item : planItems) {
                PortalMyExperimentVO vo = new PortalMyExperimentVO();
                vo.setExperimentId(item.getId());
                vo.setPlanId(reg.getPlanId());
                vo.setExperimentName(item.getExpName());
                if (plan != null) {
                    vo.setCourseId(plan.getCourseId());
                    vo.setCourseName(plan.getCourseName());
                    vo.setSemester(plan.getSemester());
                    vo.setClassName(plan.getClassName());
                    vo.setTeacherName(plan.getTeacherName());
                    vo.setLabRoom(plan.getLabRoom());
                    vo.setStartDate(plan.getStartDate());
                    vo.setEndDate(plan.getEndDate());
                }
                // 实验状态机:基于 plan.startDate/endDate + assignment.status + score 综合判定
                // (item.classDate 不再作为单一信号,避免"未开始"和"已打分"冲突)
                vo.setStatus(ExperimentStatus.compute(
                        plan != null ? plan.getStartDate() : null,
                        plan != null ? plan.getEndDate() : null,
                        reg.getStatus(),
                        reg.getScore(),
                        today
                ).getCode());
                // 把 assignment.status 也透出(供前端列表展示"已完成"等)
                vo.setAssignmentStatus(reg.getStatus() == null ? null : reg.getStatus().toString());
                all.add(vo);
            }
        }

        // 4) 关键字 / 状态过滤
        if (StringUtils.isNotBlank(keyword)) {
            String kw = keyword.trim();
            all = all.stream().filter(v ->
                    (v.getExperimentName() != null && v.getExperimentName().contains(kw))
                            || (v.getCourseName() != null && v.getCourseName().contains(kw))
            ).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(status) && !"all".equalsIgnoreCase(status)) {
            all = all.stream().filter(v -> status.equalsIgnoreCase(v.getStatus())).collect(Collectors.toList());
        }

        // 5) 内存分页
        long total = all.size();
        long from = Math.max(0L, (pageNum - 1) * pageSize);
        long to = Math.min(total, from + pageSize);
        List<PortalMyExperimentVO> slice = from >= total ? Collections.emptyList()
                : new ArrayList<>(all.subList((int) from, (int) to));
        Page<PortalMyExperimentVO> result = new Page<>(pageNum, pageSize, total);
        result.setRecords(slice);
        return result;
    }

    @Override
    public PortalExperimentDetailVO getExperimentDetail(Long studentId, Long itemId) {
        requireStudent(studentId);
        if (itemId == null) {
            throw new BusinessException("实验明细ID不能为空");
        }

        // 1) 校验 item 存在
        ExperimentPlanItem item = experimentPlanItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        ExperimentPlan plan = experimentPlanMapper.selectById(item.getPlanId());

        // 2) 校验当前学生确实被分配过这个 plan（防止越权查看未分配的实验）
        ExperimentAssignment reg = experimentAssignmentMapper.selectOne(new LambdaQueryWrapper<ExperimentAssignment>()
                .eq(ExperimentAssignment::getStudentId, studentId)
                .eq(ExperimentAssignment::getPlanId, item.getPlanId())
                .in(ExperimentAssignment::getStatus, 1, 3)
                .last("LIMIT 1"));
        if (reg == null) {
            throw new BusinessException("该实验未分配给你，无法查看详情");
        }

        // 3) 装配
        PortalExperimentDetailVO vo = new PortalExperimentDetailVO();
        vo.setItemId(item.getId());
        vo.setPlanId(item.getPlanId());
        vo.setExpNo(item.getExpNo());
        vo.setExpName(item.getExpName());
        vo.setExpType(item.getExpType());
        vo.setPurpose(item.getPurpose());
        vo.setContent(item.getContent());
        vo.setRequirement(item.getRequirement());
        vo.setResourceId(item.getResourceId());
        vo.setClassDate(item.getClassDate());
        vo.setClassPeriod(item.getClassPeriod());
        vo.setHours(item.getHours());
        vo.setTeacherName(item.getTeacherName());
        vo.setRemark(item.getRemark());

        if (plan != null) {
            vo.setPlanTitle(plan.getPlanTitle());
            vo.setCourseId(plan.getCourseId());
            vo.setCourseName(plan.getCourseName());
            vo.setSemester(plan.getSemester());
            vo.setClassName(plan.getClassName());
            vo.setLabRoom(plan.getLabRoom());
            vo.setPlanStartDate(plan.getStartDate());
            vo.setPlanEndDate(plan.getEndDate());
        }

        vo.setAssigned(true);
        vo.setAssignmentStatus(reg.getStatus());
        vo.setScore(reg.getScore());
        vo.setComment(reg.getComment());
        // 实验状态机:基于 plan.startDate/endDate + assignment.status + score 综合判定
        vo.setStatus(ExperimentStatus.compute(
                plan != null ? plan.getStartDate() : null,
                plan != null ? plan.getEndDate() : null,
                reg.getStatus(),
                reg.getScore(),
                LocalDate.now()
        ).getCode());
        return vo;
    }

    // (computeItemStatus 已删除 —— 由 ExperimentStatus.compute 替代)

    // ====================================================================
    //  反 IDOR 工具
    // ====================================================================
    private void requireStudent(Long studentId) {
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    @Override
    public void assertAssignmentVisibleToStudent(Long assignmentId, Long studentId) {
        requireStudent(studentId);
        if (assignmentId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        // 1) 学生班级
        List<SysUserClass> userClasses = sysUserClassMapper.selectList(
                new LambdaQueryWrapper<SysUserClass>().eq(SysUserClass::getUserId, studentId));
        if (userClasses.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        Set<Long> classIds = userClasses.stream()
                .map(SysUserClass::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 2) 作业目标班级
        List<SysAssignmentClass> acList = sysAssignmentClassMapper.selectList(
                new LambdaQueryWrapper<SysAssignmentClass>().eq(SysAssignmentClass::getAssignmentId, assignmentId));
        boolean visible = acList.stream()
                .map(SysAssignmentClass::getClassId)
                .filter(Objects::nonNull)
                .anyMatch(classIds::contains);
        if (!visible) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    // ====================================================================
    //  VO 装配
    // ====================================================================
    private PortalMyCourseVO toCourseVO(CourseEnrollment e) {
        PortalMyCourseVO vo = new PortalMyCourseVO();
        vo.setCourseId(e.getCourseId());
        vo.setCourseName(e.getCourseName());
        vo.setCourseCode(e.getCourseCode());
        vo.setTeacherName(e.getTeacherName());
        vo.setCoverImage(e.getCoverImage());
        vo.setTotalHours(e.getTotalHours());
        vo.setCourseType(e.getCourseType());
        vo.setCourseTypeLabel(COURSE_TYPE_LABEL.getOrDefault(e.getCourseType(), "课程"));
        vo.setProgress(clampPercent(e.getProgress()));
        vo.setEnrolledAt(e.getEnrolledAt());
        vo.setStatus(e.getStatus());
        return vo;
    }

    private PortalMyAssignmentVO toAssignmentVO(Assignment a, AssignmentSubmission latest) {
        PortalMyAssignmentVO vo = new PortalMyAssignmentVO();
        vo.setAssignmentId(a.getId());
        vo.setCourseId(a.getCourseId());
        vo.setTitle(a.getTitle());
        vo.setDescription(a.getDescription());
        vo.setTotalScore(a.getTotalScore());
        // assignment.status: 0=草稿 1=已发布 2=已截止 —— 学生列表只查 1/2,这里按需映射
        vo.setAssignmentStatus(a.getStatus() != null && a.getStatus() == 1 ? "open" : "closed");
        vo.setDeadline(a.getDeadline());

        if (latest == null) {
            vo.setStatus("pending");
        } else {
            Integer s = latest.getStatus();
            if (s != null && s == 2) {
                vo.setStatus("graded");
                vo.setScore(latest.getScore());
                vo.setGradedAt(latest.getGradeTime());
            } else if (s != null && s == 1) {
                vo.setStatus("submitted");
                vo.setSubmittedAt(latest.getSubmitTime());
            } else {
                vo.setStatus("pending");
            }
        }
        return vo;
    }

    private PortalMyTrainingVO toTrainingVO(TrainingRegistration r, TrainingPlan plan, LocalDate today) {
        PortalMyTrainingVO vo = new PortalMyTrainingVO();
        vo.setRegistrationId(r.getId());
        vo.setTrainingId(r.getPlanId());
        vo.setPlanName(r.getPlanTitle());
        vo.setClassName(r.getClassName());
        if (plan != null) {
            vo.setProjectName(plan.getProjectName());
            vo.setSemester(plan.getSemester());
            vo.setTeacherName(plan.getTeacherName());
            vo.setStartDate(plan.getStartDate());
            vo.setEndDate(plan.getEndDate());
        }

        // 待审核 (status=0):学生门户自报名后,管理员尚未审核 —— 优先级最高,先于按日期推算
        if (r.getStatus() != null && r.getStatus() == 0) {
            vo.setStatus("pending_review");
            vo.setProgress(0);
            return vo;
        }
        // 已驳回 (status=2):门户 SQL 已经过滤掉,这里做防御性映射
        if (r.getStatus() != null && r.getStatus() == 2) {
            vo.setStatus("rejected");
            vo.setProgress(0);
            return vo;
        }

        // 已完成 (status=3) -> done
        if (r.getStatus() != null && r.getStatus() == 3) {
            vo.setStatus("done");
            vo.setProgress(100);
            return vo;
        }

        // 其它按日期推算
        LocalDate start = plan != null ? plan.getStartDate() : null;
        LocalDate end   = plan != null ? plan.getEndDate()   : null;
        if (start == null || end == null) {
            vo.setStatus("not_started");
            vo.setProgress(0);
            return vo;
        }
        if (today.isBefore(start)) {
            vo.setStatus("not_started");
            vo.setProgress(0);
        } else if (today.isAfter(end)) {
            vo.setStatus("done");
            vo.setProgress(100);
        } else {
            vo.setStatus("in_progress");
            vo.setProgress(percentBetween(start, end, today));
        }
        return vo;
    }

    // ====================================================================
    //  工具
    // ====================================================================
    private boolean isMoreAdvanced(AssignmentSubmission cur, AssignmentSubmission prev) {
        int cp = priorityOf(cur.getStatus());
        int pp = priorityOf(prev.getStatus());
        if (cp != pp) return cp > pp;
        LocalDateTime ct = cur.getSubmitTime() != null ? cur.getSubmitTime() : cur.getCreateTime();
        LocalDateTime pt = prev.getSubmitTime() != null ? prev.getSubmitTime() : prev.getCreateTime();
        if (ct == null || pt == null) return cur.getId() > prev.getId();
        return ct.isAfter(pt);
    }
    private int priorityOf(Integer s) {
        if (s == null) return 0;
        switch (s) {
            case 2: return 3;     // 已批改
            case 1: return 2;     // 已提交
            default: return 1;    // 草稿
        }
    }

    private boolean matchesStatus(String actual, String want) {
        // String.isBlank() 是 Java 11+;项目目标为 Java 10,改用 Commons Lang3 兜底
        if (want == null || StringUtils.isBlank(want) || "all".equalsIgnoreCase(want)) return true;
        return want.equalsIgnoreCase(actual);
    }
    private boolean matchesTrainingStatus(String actual, String want) {
        return matchesStatus(actual, want);
    }

    private Integer clampPercent(Integer p) {
        if (p == null) return 0;
        if (p < 0) return 0;
        if (p > 100) return 100;
        return p;
    }

    private int percentBetween(LocalDate start, LocalDate end, LocalDate today) {
        long total = ChronoUnit.DAYS.between(start, end);
        if (total <= 0) return 100;
        long passed = ChronoUnit.DAYS.between(start, today);
        long clamped = Math.max(0, Math.min(total, passed));
        return (int) Math.round(clamped * 100.0 / total);
    }
}
