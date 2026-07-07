package com.smartteach.modules.course.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseQueryDTO extends PageQuery {
    /** 综合搜索关键字：同时匹配 courseCode / courseName */
    private String keyword;
    private String courseCode;
    private String courseName;
    private Long categoryId;
    private Long teacherId;
    private Integer courseType;
    private Integer status;
}
