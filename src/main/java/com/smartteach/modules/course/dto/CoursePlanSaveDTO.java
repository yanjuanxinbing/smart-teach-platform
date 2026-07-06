package com.smartteach.modules.course.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class CoursePlanSaveDTO {
    private Long id;

    @NotBlank(message = "计划标题不能为空")
    private String planTitle;

    @NotNull(message = "课程不能为空")
    private Long courseId;

    private String courseName;

    @NotBlank(message = "学期不能为空")
    private String semester;

    private String className;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalWeeks;
    private String description;
    private Integer status;

    /** 计划明细（按周次拆分），可随主表一起保存 */
    @Valid
    private List<CoursePlanItemDTO> items;
}
