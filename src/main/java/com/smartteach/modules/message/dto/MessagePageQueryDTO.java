package com.smartteach.modules.message.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息分页查询 DTO
 * tab: all / unread / system / course / private ...
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessagePageQueryDTO extends PageQuery {
    private String tab;
    private String type;
}
