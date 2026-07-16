package com.smartteach.modules.aiassignment.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 按月时间序列点（班级分析）
 */
@Data
public class MonthlyPointVO {

    /** 月份字符串 "2025-09" */
    private String month;

    private Long gradedCount;
    private BigDecimal avgScore;
}
