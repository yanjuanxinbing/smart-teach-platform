package com.smartteach.modules.profile.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 我的操作日志查询 DTO（pageNum / size 已在 PageQuery 中提供）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MyLogQueryDTO extends PageQuery {
    /** 类型筛选：login / profile / course / assignment / code 等 */
    private String type;
    /** 关键字：动作/模块/IP 模糊匹配 */
    private String keyword;
    /** 时间起 / 止（yyyy-MM-dd HH:mm:ss） */
    private String startTime;
    private String endTime;
}
