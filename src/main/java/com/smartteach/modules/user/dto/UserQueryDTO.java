package com.smartteach.modules.user.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQuery {
    private String username;
    private String realName;
    private Integer status;
    private Long deptId;
}
