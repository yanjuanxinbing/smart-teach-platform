package com.smartteach.modules.aiassignment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 班级维度作业统计
 */
@Data
public class ClassAssignmentStatsVO {

    private Long classId;
    private String className;
    /** 推断或入参的学期字符串，如 "2025-2026-1" */
    private String semester;

    private Long assignmentCount;
    private Long expectedSubmissions;
    private Long submittedCount;
    private BigDecimal avgScore;
    private Long lateCount;

    /** 5 个分数段人数（已批改 status=2） */
    private Long bucket90;
    private Long bucket80;
    private Long bucket70;
    private Long bucket60;
    private Long bucketFail;

    /** 未提交 = 应提交 - 实际提交；服务端推算 */
    private Long bucketUnsubmitted;

    private List<AssignmentAvgScoreVO> assignmentAvgScores;
    private List<MonthlyPointVO> monthlyTrend;
}
