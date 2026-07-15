package com.smartteach.modules.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class CourseSaveDTO {
    private Long id;

    @NotBlank(message = "课程编号不能为空")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    // 课程分类已从课程管理移除，保留字段仅为兼容历史数据（可空）
    private Long categoryId;

    private String categoryName;
    private String description;

    // 任课教师改由「授课管理」维护，此处不再必填（可空）
    private Long teacherId;

    private String teacherName;
    private BigDecimal credit;
    private Integer totalHours;
    private Integer courseType;
    private Integer status;
}
