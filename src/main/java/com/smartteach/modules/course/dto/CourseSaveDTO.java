package com.smartteach.modules.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CourseSaveDTO {
    private Long id;

    @NotBlank(message = "课程编号不能为空")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    @NotNull(message = "课程分类不能为空")
    private Long categoryId;

    private String categoryName;
    private String description;
    private String coverImage;

    @NotNull(message = "任课教师不能为空")
    private Long teacherId;

    private String teacherName;
    private BigDecimal credit;
    private Integer totalHours;
    private Integer courseType;
    private Integer status;
}
