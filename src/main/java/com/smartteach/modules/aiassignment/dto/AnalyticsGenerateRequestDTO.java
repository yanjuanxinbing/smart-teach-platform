package com.smartteach.modules.aiassignment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * AI 报告生成入参：
 *  - 教师侧：classId 必传
 *  - 学生侧：studentId 不必传，后端从 UserContext 取本人
 */
@Data
public class AnalyticsGenerateRequestDTO {

    @NotNull(message = "班级 ID 不能为空")
    private Long classId;

    /** 可选追问；为空则用默认问题 */
    @Size(max = 500, message = "追问不超过 500 字")
    private String question;

    /** 可选学期字符串，如 "2025-2026-1"；缺省按 Calendar 推断 */
    private String semester;
}
