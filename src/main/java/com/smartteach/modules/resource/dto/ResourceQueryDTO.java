package com.smartteach.modules.resource.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceQueryDTO extends PageQuery {
    private String resourceName;
    private Integer resourceType;
    private Long categoryId;
    private Long courseId;
    private String tags;
    private Integer status;
}
