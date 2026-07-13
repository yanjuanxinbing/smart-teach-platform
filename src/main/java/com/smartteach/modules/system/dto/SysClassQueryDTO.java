package com.smartteach.modules.system.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysClassQueryDTO extends PageQuery {

    private Long deptId;

    /** 模糊匹配班级名 */
    private String keyword;
}