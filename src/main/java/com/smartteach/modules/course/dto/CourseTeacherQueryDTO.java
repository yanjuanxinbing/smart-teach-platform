package com.smartteach.modules.course.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseTeacherQueryDTO extends PageQuery {

    private Long courseId;
    private Long teacherId;

    /** 模糊匹配课程名 / 课程编号 / 教师姓名 */
    private String keyword;

    /** 主讲 / 助教 */
    private String role;

    /** 0禁用 1启用 */
    private Integer status;
}