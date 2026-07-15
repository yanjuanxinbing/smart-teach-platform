package com.smartteach.modules.aiassistant.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单条问答消息
 */
@Data
public class AiMessageVO {

    private Long id;

    private Long sessionId;

    /** user / assistant */
    private String role;

    private String content;

    private Integer durationMs;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
