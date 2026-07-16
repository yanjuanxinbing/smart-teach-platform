package com.smartteach.modules.aiassignment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生单个分数点（学生分析）
 */
@Data
public class StudentScorePointVO {

    private Long assignmentId;
    private String title;
    private LocalDateTime deadline;
    private BigDecimal score;
}
