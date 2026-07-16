package com.smartteach.modules.aiassistant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 解题会话（一次会话 = 同一主题下的多轮问答）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("aiassistant_session")
public class AiAssistantSession extends BaseEntity {

    /** 学生/教师用户ID */
    private Long userId;

    /** 会话标题（首问前 30 字） */
    private String title;

    /** 0关闭 1进行中 */
    private Integer status;
}
