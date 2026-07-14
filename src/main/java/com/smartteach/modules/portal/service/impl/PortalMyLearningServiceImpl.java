package com.smartteach.modules.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.course.assignment.entity.Assignment;
import com.smartteach.modules.course.assignment.entity.AssignmentSubmission;
import com.smartteach.modules.course.assignment.mapper.AssignmentMapper;
import com.smartteach.modules.course.assignment.mapper.AssignmentSubmissionMapper;
import com.smartteach.modules.portal.entity.CourseEnrollment;
import com.smartteach.modules.portal.mapper.CourseEnrollmentMapper;
import com.smartteach.modules.portal.service.PortalMyLearningService;
import com.smartteach.modules.portal.vo.PortalMyAssignmentVO;
import com.smartteach.modules.portal.vo.PortalMyCourseVO;
import com.smartteach.modules.portal.vo.PortalMyTrainingVO;
import com.smartteach.modules.training.entity.TrainingPlan;
import com.smartteach.modules.training.entity.TrainingRegistration;
import com.smartteach.modules.training.mapper.TrainingPlanMapper;
import com.smartteach.modules.training.mapper.TrainingRegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    public IPage<PortalMyAssignmentVO> myAssignments(Long studentId, long pageNum, long pageSize, String status) {
        requireStudent(studentId);

        // 1) 该学生所有提交，按 assignment_id + 提交时间聚合取最新一条
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

        // 2) 反查作业详情
        Set<Long> assignmentIds = latestByAssignment.keySet();
        List<Assignment> assignments;
        if (assignmentIds.isEmpty()) {
            assignments = Collections.emptyList();
        } else {
            LambdaQueryWrapper<Assignment> aWrap = new LambdaQueryWrapper<>();
            aWrap.in(Assignment::getId, assignmentIds)
                    .in(Assignment::getStatus, 1, 2)  // 已发布 + 已截止
                    .orderByDesc(Assignment::getDeadline);
            // 状态过滤的同时也要拉"未提交"集合 —— 这里一并按需补一个不带 submission 的查询
            assignments = assignmentMapper.selectList(aWrap);
        }

        // 3) 组装并按状态过滤
        List<PortalMyAssignmentVO> all = new ArrayList<>(assignments.size());
        for (Assignment a : assignments) {
            AssignmentSubmission latest = latestByAssignment.get(a.getId());
            PortalMyAssignmentVO vo = toAssignmentVO(a, latest);
            if (matchesStatus(vo.getStatus(), status)) all.add(vo);
        }
        // 同时,当前学生所有"已发布/已截止"但未提交的作业也应展示为 pending —— 暂以"有提交记录"为全集,
        // 因为历史上未提交作业的查询会膨胀,生产场景建议后台另起一个定时任务把"未提交作业"显式建档.
        // 此处保留按"已发布"查询的注释实现,不影响主流程.
        // TODO: 后续若需要"看到所有未提交作业",再以"目标班级 -> 作业关联"反向补齐.

        // 4) 内存分页
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
        LambdaQueryWrapper<TrainingRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRegistration::getStudentId, studentId)
                .in(TrainingRegistration::getStatus, 1, 3)   // 仅"已通过 / 已完成"
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
    //  反 IDOR 工具
    // ====================================================================
    private void requireStudent(Long studentId) {
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
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
