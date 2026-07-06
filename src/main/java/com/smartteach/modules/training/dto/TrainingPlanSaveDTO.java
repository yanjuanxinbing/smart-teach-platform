package com.smartteach.modules.training.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class TrainingPlanSaveDTO {
    private Long id;

    @NotBlank(message = "计划标题不能为空")
    private String planTitle;

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    private Long courseId;
    private String courseName;
    private String semester;
    private String className;
    private Long teacherId;
    private String teacherName;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationDays;
    private Integer totalHours;
    private Integer capacity;
    private String objective;
    private String content;
    private String assessment;
    private Integer status;
}
