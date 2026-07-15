package com.smartteach.modules.aiassistant.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 同步提问入参（/solve 接口使用；流式 /stream 接口走 @RequestParam）
 */
@Data
public class AiSolveRequestDTO {

    @NotBlank(message = "问题不能为空")
    @Size(min = 2, max = 2000, message = "问题长度需在 2~2000 字符之间")
    private String prompt;

    /** 可选：继续某次会话 */
    private Long sessionId;
}
