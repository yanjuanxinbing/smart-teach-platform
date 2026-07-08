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
    /** 按创建人（教师）过滤；为空表示查全部 */
    private Long teacherId;
    /** mine=true 时只查当前登录用户创建的计划 */
    private Boolean mine;
}