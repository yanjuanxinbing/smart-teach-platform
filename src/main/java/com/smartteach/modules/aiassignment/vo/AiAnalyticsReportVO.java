package com.smartteach.modules.aiassignment.vo;

import lombok.Data;

/**
 * AI 报告外壳（含元数据）
 */
@Data
public class AiAnalyticsReportVO {

    /** AI 生成的整篇报告（已含 \n 换行符，前端 v-html 用 <br/> 渲染） */
    private String report;

    private String model;
    private Long promptEvalCount;
    private Long evalCount;
    private Long durationMs;

    /** 当前统计的维度："class" 或 "student" */
    private String scope;
    /** 关联的 id：classId 或 studentId */
    private Long targetId;
    private String semester;
}
