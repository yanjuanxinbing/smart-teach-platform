package com.smartteach.modules.experiment.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExperimentPlanSaveDTO {
    private Long id;

    @NotBlank(message = "计划标题不能为空")
    private String planTitle;

    @NotNull(message = "课程不能为空")
    private Long courseId;

    private String courseName;
    private String semester;
    private String className;
    private Long teacherId;
    private String teacherName;
    private String labRoom;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalExperiments;
    private Integer totalHours;
    private String description;
    private Integer status;

    @Valid
    private List<ExperimentPlanItemDTO> items;
}
