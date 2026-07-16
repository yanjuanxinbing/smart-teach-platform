package com.smartteach.modules.aiassignment.service.impl;

import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.aiassistant.client.OllamaClient;
import com.smartteach.modules.aiassistant.client.OllamaClient.ChatMessage;
import com.smartteach.modules.aiassistant.client.OllamaClient.OllamaResponse;
import com.smartteach.modules.aiassignment.dto.AnalyticsGenerateRequestDTO;
import com.smartteach.modules.aiassignment.formatter.AnalyticsPromptFormatter;
import com.smartteach.modules.aiassignment.mapper.AnalyticsMapper;
import com.smartteach.modules.aiassignment.properties.AiAssignmentProperties;
import com.smartteach.modules.aiassignment.service.AssignmentAnalyticsService;
import com.smartteach.modules.aiassignment.vo.AiAnalyticsReportVO;
import com.smartteach.modules.aiassignment.vo.ClassAssignmentStatsVO;
import com.smartteach.modules.aiassignment.vo.StudentAssignmentStatsVO;
import com.smartteach.modules.system.entity.SysClass;
import com.smartteach.modules.system.mapper.SysClassMapper;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * aiassignment Service 实现：
 *  - 调 AnalyticsMapper 出各类聚合数据
 *  - 包装成 ClassAssignmentStatsVO / StudentAssignmentStatsVO
 *  - 调 OllamaClient.chat(...) 拿 AI 报告
 *  - 日期窗：入参 semester 优先；缺省按 Calendar 推断
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentAnalyticsServiceImpl implements AssignmentAnalyticsService {

    private final AnalyticsMapper analyticsMapper;
    private final OllamaClient ollamaClient;
    private final AiAssignmentProperties props;
    private final AnalyticsPromptFormatter formatter;
    private final SysClassMapper classMapper;
    private final SysUserMapper userMapper;

    // ================================================================
    // 班级维度
    // ================================================================

    @Override
    public ClassAssignmentStatsVO classStats(Long classId, String semester) {
        DateRange r = resolveSemester(semester);
        SysClass sysClass = classMapper.selectById(classId);
        String className = sysClass == null ? "未知班级" : sysClass.getClassName();

        Map<String, Object> overview = analyticsMapper.classOverview(classId, r.start, r.end);
        Map<String, Object> buckets  = analyticsMapper.classScoreBuckets(classId, r.start, r.end);

        ClassAssignmentStatsVO vo = new ClassAssignmentStatsVO();
        vo.setClassId(classId);
        vo.setClassName(className);
        vo.setSemester(r.label);

        vo.setAssignmentCount(toLong(overview == null ? null : overview.get("assignment_count")));
        vo.setExpectedSubmissions(toLong(overview == null ? null : overview.get("expected_submissions")));
        vo.setSubmittedCount(toLong(overview == null ? null : overview.get("submitted_count")));
        vo.setLateCount(toLong(overview == null ? null : overview.get("late_count")));

        vo.setBucket90(toLong(buckets == null ? null : buckets.get("b90")));
        vo.setBucket80(toLong(buckets == null ? null : buckets.get("b80")));
        vo.setBucket70(toLong(buckets == null ? null : buckets.get("b70")));
        vo.setBucket60(toLong(buckets == null ? null : buckets.get("b60")));
        vo.setBucketFail(toLong(buckets == null ? null : buckets.get("bFail")));

        Double avg = analyticsMapper.classAvgScore(classId, r.start, r.end);
        vo.setAvgScore(avg == null ? null : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));

        long unsubmitted = Math.max(0L,
                (vo.getExpectedSubmissions() == null ? 0L : vo.getExpectedSubmissions())
              - (vo.getSubmittedCount() == null ? 0L : vo.getSubmittedCount()));
        vo.setBucketUnsubmitted(unsubmitted);

        vo.setAssignmentAvgScores(analyticsMapper.classAssignmentAvgScores(classId, r.start, r.end));
        vo.setMonthlyTrend(analyticsMapper.classMonthlyTrend(classId, r.start, r.end));
        return vo;
    }

    @Override
    public AiAnalyticsReportVO generateClassReport(AnalyticsGenerateRequestDTO dto) {
        ClassAssignmentStatsVO stats = classStats(dto.getClassId(), dto.getSemester());
        String user = formatter.formatClassForPrompt(stats, dto.getQuestion());
        log.info("[AI analytics] class report classId={}, promptLen={}", dto.getClassId(), user.length());
        OllamaResponse resp = ollamaClient.chat(List.of(
                ChatMessage.system(props.getClassSystemPrompt()),
                ChatMessage.user(user)
        ));
        return toReportVO(resp, "class", dto.getClassId(), stats.getSemester());
    }

    // ================================================================
    // 学生维度
    // ================================================================

    @Override
    public StudentAssignmentStatsVO studentStats(Long studentId, String semester) {
        DateRange r = resolveSemester(semester);
        SysUser user = userMapper.selectById(studentId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        Map<String, Object> ov = analyticsMapper.studentOverview(studentId, r.start, r.end);
        StudentAssignmentStatsVO vo = new StudentAssignmentStatsVO();
        vo.setStudentId(studentId);
        vo.setStudentName(user.getRealName() == null ? user.getUsername() : user.getRealName());
        vo.setClassName(user.getRemark());
        vo.setSemester(r.label);

        vo.setAssignmentCount(toLong(ov == null ? null : ov.get("assignment_count")));
        vo.setSubmittedCount(toLong(ov == null ? null : ov.get("submitted_count")));
        vo.setGradedCount(toLong(ov == null ? null : ov.get("graded_count")));
        vo.setLateCount(toLong(ov == null ? null : ov.get("late_count")));
        vo.setAvgScore(toBigDecimal(ov == null ? null : ov.get("avg_score")));
        vo.setMaxScore(toBigDecimal(ov == null ? null : ov.get("max_score")));
        vo.setMinScore(toBigDecimal(ov == null ? null : ov.get("min_score")));
        vo.setStdScore(toBigDecimal(ov == null ? null : ov.get("std_score")));

        // 未完成 = 作业总数 - 已批改
        long ac = vo.getAssignmentCount() == null ? 0L : vo.getAssignmentCount();
        long gc = vo.getGradedCount() == null ? 0L : vo.getGradedCount();
        vo.setUnsubmittedCount(Math.max(0L, ac - gc));

        vo.setScoreTrend(analyticsMapper.studentScoreTrend(studentId, r.start, r.end));
        vo.setUnsubmittedList(analyticsMapper.studentUnsubmitted(studentId, r.start, r.end));
        return vo;
    }

    @Override
    public AiAnalyticsReportVO generateStudentReport(AnalyticsGenerateRequestDTO dto) {
        Long studentId = UserContext.getUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        StudentAssignmentStatsVO stats = studentStats(studentId, dto.getSemester());
        String user = formatter.formatStudentForPrompt(stats, dto.getQuestion());
        log.info("[AI analytics] student report studentId={}, promptLen={}", studentId, user.length());
        OllamaResponse resp = ollamaClient.chat(List.of(
                ChatMessage.system(props.getStudentSystemPrompt()),
                ChatMessage.user(user)
        ));
        return toReportVO(resp, "student", studentId, stats.getSemester());
    }

    // ================================================================
    // 内部
    // ================================================================

    private AiAnalyticsReportVO toReportVO(OllamaResponse resp, String scope, Long targetId, String semester) {
        AiAnalyticsReportVO vo = new AiAnalyticsReportVO();
        vo.setReport(resp.getContent());
        vo.setModel(resp.getModel());
        vo.setPromptEvalCount(resp.getPromptEvalCount());
        vo.setEvalCount(resp.getEvalCount());
        vo.setDurationMs(resp.getTotalDurationNs() == null ? null : resp.getTotalDurationNs() / 1_000_000L);
        vo.setScope(scope);
        vo.setTargetId(targetId);
        vo.setSemester(semester);
        return vo;
    }

    private static Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return null; }
    }

    /** "2025-2026-1" / "2025-2026-2" → DateRange；缺省按当前日期推断当前/最近结束的学期 */
    private static final Pattern SEMESTER_PATTERN = Pattern.compile("(\\d{4})-(\\d{4})-([12])");

    /**
     * 学期推断规则（与中国高校常见学期对齐）：
     *   - 9 月 ~ 12 月：在「秋季学期（-1）」中，窗口 {Sep 1, y} ~ {Jan 31, y+1}
     *   - 1 月      ：仍在上一秋季学期（寒假期间），同上
     *   - 2 月 ~ 6 月：在「春季学期（-2）」中，窗口 {Feb 1, y} ~ {Jul 31, y}
     *   - 7 月 ~ 8 月：暑假，使用「刚结束的春季学期」，同上
     *
     * 窗口长度统一 +6 个月，避免月初跨边界（如 10104 deadline 在 7/2，仍能落入"上学期"的尾部）。
     */
    static DateRange resolveSemester(String s) {
        if (s == null || s.isBlank()) {
            LocalDate today = LocalDate.now();
            int y = today.getYear();
            int m = today.getMonthValue();
            String label;
            LocalDate start;
            if (m >= 9 || m == 1) {
                // 秋季学期：m=1 → (y-1, y)；m >= 9 → (y, y+1)
                int first  = (m >= 9) ? y     : y - 1;
                int second = (m >= 9) ? y + 1 : y;
                label  = first + "-" + second + "-1";
                start  = LocalDate.of(first, 9, 1);
            } else {
                // m = 2..8：春季学期（y-1, y）
                int first  = y - 1;
                int second = y;
                label  = first + "-" + second + "-2";
                start  = LocalDate.of(second, 2, 1);
            }
            LocalDate end = start.plusMonths(6).with(TemporalAdjusters.lastDayOfMonth());
            return new DateRange(label, start.atStartOfDay(), end.atTime(LocalTime.MAX));
        }

        Matcher m = SEMESTER_PATTERN.matcher(s.trim());
        if (m.matches()) {
            int first  = Integer.parseInt(m.group(1));
            int second = Integer.parseInt(m.group(2));
            int which  = Integer.parseInt(m.group(3));
            if (first + 1 != second) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            LocalDate start;
            if (which == 1) {
                start = LocalDate.of(first, 9, 1);
            } else {
                start = LocalDate.of(second, 2, 1);
            }
            LocalDate end = start.plusMonths(6).with(TemporalAdjusters.lastDayOfMonth());
            return new DateRange(s.trim(), start.atStartOfDay(), end.atTime(LocalTime.MAX));
        }
        throw new BusinessException(ResultCode.PARAM_ERROR);
    }

    static class DateRange {
        final String label;
        final LocalDateTime start;
        final LocalDateTime end;
        DateRange(String label, LocalDateTime start, LocalDateTime end) {
            this.label = label;
            this.start = start;
            this.end = end;
        }
    }

    @SuppressWarnings("unused")
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
}
