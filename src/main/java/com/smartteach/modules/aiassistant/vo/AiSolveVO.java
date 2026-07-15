package com.smartteach.modules.aiassistant.vo;

import lombok.Data;

/**
 * 同步提问出参
 */
@Data
public class AiSolveVO {

    /** 会话ID（首次提问返回新建的 ID） */
    private Long sessionId;

    /** AI 回答正文 */
    private String answer;

    /** 实际使用的模型名（如 gemma3:1b） */
    private String model;

    /** 耗时（毫秒） */
    private Long durationMs;

    /** 用户原问题（回显） */
    private String prompt;

    /** server 端时间戳毫秒 */
    private Long createdAt;
}
