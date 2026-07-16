package com.smartteach.modules.aiassignment.formatter;

import com.smartteach.modules.aiassignment.properties.AiAssignmentProperties;
import com.smartteach.modules.aiassignment.vo.ClassAssignmentStatsVO;
import com.smartteach.modules.aiassignment.vo.StudentAssignmentStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 把统计 VO 序列化成发给模型的 "用户消息" 文本。统一截断到 properties.maxPromptChars。
 *
 * <p>风格：纯文本 + 换行 + 冒号，避免任何 markdown 标记（system prompt 也禁 markdown）。
 */
@Component
@RequiredArgsConstructor
public class AnalyticsPromptFormatter {

    private final AiAssignmentProperties props;

    public String formatClassForPrompt(ClassAssignmentStatsVO s, String userQuestion) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("## 班级数据\n");
        sb.append("班级：").append(s.getClassName()).append('\n');
        sb.append("时间范围：").append(s.getSemester()).append('\n');
        sb.append("作业总数：").append(s.getAssignmentCount())
          .append("；应提交人次：").append(s.getExpectedSubmissions())
          .append("；实际提交人次：").append(s.getSubmittedCount())
          .append("；平均分：").append(formatNum(s.getAvgScore())).append('\n');
        sb.append("分数段：90+/80-89/70-79/60-69/60-以下/未提交 = ")
          .append(s.getBucket90() == null ? 0 : s.getBucket90()).append('/')
          .append(s.getBucket80() == null ? 0 : s.getBucket80()).append('/')
          .append(s.getBucket70() == null ? 0 : s.getBucket70()).append('/')
          .append(s.getBucket60() == null ? 0 : s.getBucket60()).append('/')
          .append(s.getBucketFail() == null ? 0 : s.getBucketFail()).append('/')
          .append(s.getBucketUnsubmitted() == null ? 0 : s.getBucketUnsubmitted()).append('\n');
        sb.append("滞交人次：").append(s.getLateCount()).append('\n');
        if (s.getMonthlyTrend() != null && !s.getMonthlyTrend().isEmpty()) {
            sb.append("按月趋势：");
            s.getMonthlyTrend().forEach(m ->
                sb.append(m.getMonth()).append("(均分").append(formatNum(m.getAvgScore())).append(", 已批改").append(m.getGradedCount()).append(") "));
            sb.append('\n');
        }
        if (s.getAssignmentAvgScores() != null && !s.getAssignmentAvgScores().isEmpty()) {
            sb.append("逐作业平均分：");
            s.getAssignmentAvgScores().forEach(a ->
                sb.append(a.getTitle()).append('=').append(formatNum(a.getAvgScore())).append("(提交").append(a.getSubmittedCount()).append("); "));
            sb.append('\n');
        }
        sb.append("\n老师追问：").append((userQuestion == null || userQuestion.isBlank()) ? "（无）" : userQuestion);
        return clamp(sb.toString(), props.getMaxPromptChars());
    }

    public String formatStudentForPrompt(StudentAssignmentStatsVO s, String userQuestion) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("## 我的作业数据\n");
        sb.append("姓名：").append(s.getStudentName()).append("（班级 ").append(s.getClassName()).append("）\n");
        sb.append("时间范围：").append(s.getSemester()).append('\n');
        sb.append("作业总数：").append(s.getAssignmentCount())
          .append("；已提交：").append(s.getSubmittedCount())
          .append("；已批改：").append(s.getGradedCount())
          .append("；滞交：").append(s.getLateCount())
          .append("；未完成：").append(s.getUnsubmittedCount()).append('\n');
        sb.append("分数概览（仅就批改行）：平均 ").append(formatNum(s.getAvgScore()))
          .append("；最高 ").append(formatNum(s.getMaxScore()))
          .append("；最低 ").append(formatNum(s.getMinScore()))
          .append("；标准差 ").append(formatNum(s.getStdScore())).append('\n');
        if (s.getScoreTrend() != null && !s.getScoreTrend().isEmpty()) {
            sb.append("分数按时间序列：");
            s.getScoreTrend().forEach(p ->
                sb.append('[')
                  .append(p.getTitle()).append(" - ")
                  .append(p.getDeadline() == null ? "?" : p.getDeadline().toString().substring(0, 10))
                  .append(" : ")
                  .append(formatNum(p.getScore()))
                  .append("] "));
            sb.append('\n');
        }
        if (s.getUnsubmittedList() != null && !s.getUnsubmittedList().isEmpty()) {
            sb.append("未完成清单（含草稿）：");
            s.getUnsubmittedList().forEach(u ->
                sb.append(u.getTitle())
                  .append("(").append(u.getDeadline() == null ? "?" : u.getDeadline().toString().substring(0, 10))
                  .append("); "));
            sb.append('\n');
        }
        sb.append("\n学生追问：").append((userQuestion == null || userQuestion.isBlank()) ? "（无）" : userQuestion);
        return clamp(sb.toString(), props.getMaxPromptChars());
    }

    private static String formatNum(BigDecimal v) {
        if (v == null) return "-";
        return v.setScale(1, RoundingMode.HALF_UP).toString();
    }

    private static String clamp(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…(已截断)";
    }
}
