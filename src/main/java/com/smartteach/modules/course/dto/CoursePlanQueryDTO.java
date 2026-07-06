package com.smartteach.modules.course.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoursePlanQueryDTO extends PageQuery {
    private String planTitle;
    private Long courseId;
    private String semester;
    private String className;
    private Integer status;
}
