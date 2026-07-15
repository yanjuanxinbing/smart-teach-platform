package com.smartteach.modules.aiassistant.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的会话列表项（左/右侧会话抽屉使用）
 */
@Data
public class AiSessionVO {

    private Long id;

    /** 会话标题 */
    private String title;

    /** 0关闭 1进行中 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 消息条数（前端可省略；本次不计算） */
    private Integer messageCount;
}
