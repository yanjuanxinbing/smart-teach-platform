package com.smartteach.common.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 实验状态(纯实时计算 —— 不落库到 experiment_assignment.status)
 *
 * 状态机:
 *   NOT_STARTED   未开始   now < startDate
 *   IN_PROGRESS   进行中   startDate <= now <= endDate
 *   PENDING_SCORE 待评分   now > endDate 且 成绩未录入
 *   COMPLETED     已完成   now > endDate 且 成绩已录入 (assignment.status==3 或 score>0)
 *
 * 用于前后端实时渲染实验进度,杜绝"未开始 + 已打分"的状态冲突。
 */
@Getter
public enum ExperimentStatus {

    NOT_STARTED("not_started", "未开始"),
    IN_PROGRESS("in_progress", "进行中"),
    PENDING_SCORE("pending_score", "待评分"),
    COMPLETED("completed", "已完成");

    private final String code;
    private final String label;

    ExperimentStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 计算当前实验状态。null 安全:任一入参为 null 时回退到 NOT_STARTED。
     *
     * @param startDate         实验计划开始日期
     * @param endDate           实验计划结束日期
     * @param assignmentStatus  分配记录状态 (experiment_assignment.status: 1已分配 / 3已完成);允许 null
     * @param score             分配记录成绩;允许 null
     * @param today             当前日期
     */
    public static ExperimentStatus compute(LocalDate startDate,
                                           LocalDate endDate,
                                           Integer assignmentStatus,
                                           BigDecimal score,
                                           LocalDate today) {
        if (startDate == null || endDate == null || today == null) {
            return NOT_STARTED;
        }
        // 进行中或之前
        if (today.isBefore(startDate)) {
            return NOT_STARTED;
        }
        if (!today.isAfter(endDate)) {
            // startDate <= today <= endDate
            return IN_PROGRESS;
        }
        // today > endDate —— 看成绩是否已录入
        boolean hasScore = score != null && score.compareTo(BigDecimal.ZERO) > 0;
        boolean explicitlyCompleted = assignmentStatus != null && assignmentStatus == 3;
        if (hasScore || explicitlyCompleted) {
            return COMPLETED;
        }
        return PENDING_SCORE;
    }
}
