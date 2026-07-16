package com.smartteach.modules.portal.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 「我的作业」学生视图 —— 学生维度的作业列表（来自班级目标 + 关联 course）
 *
 * <p>status 字段统一为前端约定的三态字符串：pending / submitted / graded</p>
 */
@Data
public class PortalMyAssignmentVO {

    /** 作业ID */
    private Long assignmentId;

    /** 课程名（冗余展示） */
    private String courseName;

    /** 课程ID */
    private Long courseId;

    /** 作业标题 */
    private String title;

    /** 截止时间 */
    private LocalDateTime deadline;

    /**
     * 学生视角状态:
     *   pending   —— 尚未提交
     *   submitted —— 已提交,等待批改
     *   graded    —— 已批改（有分数）
     */
    private String status;

    private BigDecimal score;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;

    // === 新增(任务2 学生门户作业后端) ===
    /** 作业描述(富文本/纯文本) */
    private String description;
    /** 总分(默认 100) */
    private BigDecimal totalScore;
    /** 作业本身状态:open (status=1 已发布) | closed (status=2 已截止) */
    private String assignmentStatus;
}
