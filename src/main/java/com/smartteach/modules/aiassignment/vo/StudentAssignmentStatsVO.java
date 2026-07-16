package com.smartteach.modules.aiassignment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 学生维度作业统计
 */
@Data
public class StudentAssignmentStatsVO {

    private Long studentId;
    private String studentName;
    /** 学生所属班级名（冗余便于展示） */
    private String className;
    private String semester;

    private Long assignmentCount;
    private Long submittedCount;
    private Long gradedCount;
    private Long lateCount;
    /** 提交了且被批改 或 草稿 也算未完成；服务端与 assignmentCount - gradedCount 计算 */
    private Long unsubmittedCount;

    /** 仅就 status=2 已批改行计算 */
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal stdScore;

    private List<StudentScorePointVO> scoreTrend;
    private List<UnsubmittedAssignmentVO> unsubmittedList;
}
