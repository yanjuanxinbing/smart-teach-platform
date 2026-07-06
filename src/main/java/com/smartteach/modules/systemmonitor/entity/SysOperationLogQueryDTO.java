package com.smartteach.modules.systemmonitor.entity;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysOperationLogQueryDTO extends PageQuery {
    private String module;
    private String username;
    private Integer status;
}
