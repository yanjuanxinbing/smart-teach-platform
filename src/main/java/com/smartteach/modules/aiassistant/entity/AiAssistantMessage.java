package com.smartteach.modules.aiassistant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 解题消息流水（user 与 assistant 各自一条）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("aiassistant_message")
public class AiAssistantMessage extends BaseEntity {

    /** 所属会话ID */
    private Long sessionId;

    /** 冗余存 userId 便于按用户筛 */
    private Long userId;

    /** user / assistant */
    private String role;

    /** 消息正文 */
    private String content;

    /** Ollama 返回的 prompt token 数（assistant 消息才有） */
    private Integer promptEvalCount;

    /** Ollama 返回的 eval token 数 */
    private Integer evalCount;

    /** 本次调用耗时（毫秒） */
    private Integer durationMs;
}
