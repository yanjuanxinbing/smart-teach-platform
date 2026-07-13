package com.smartteach.modules.message.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息中心
 */
@Data
@TableName("message_notify")
public class MessageNotify {

    @TableId
    private Long id;

    /** 接收人用户ID */
    private Long userId;

    /** 类型 system/system-audit/course/assignment/private/code */
    private String type;

    /** 级别 info/warn/danger/success */
    private String level;

    private String title;
    private String content;
    private String brief;
    private String targetUrl;

    /** 0未读 1已读 */
    private Integer readFlag;

    private LocalDateTime readTime;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
