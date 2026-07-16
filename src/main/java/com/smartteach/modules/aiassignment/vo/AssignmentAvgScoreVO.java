package com.smartteach.modules.aiassignment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 按作业平均分行（班级分析内的"逐作业平均分"列表元素）
 */
@Data
public class AssignmentAvgScoreVO {

    private Long assignmentId;
    private String title;
    private LocalDateTime deadline;
    private BigDecimal avgScore;
    private Long submittedCount;
}
