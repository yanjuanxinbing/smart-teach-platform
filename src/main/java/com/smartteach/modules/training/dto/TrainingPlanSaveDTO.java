package com.smartteach.modules.training.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class TrainingPlanSaveDTO {
    private Long id;

    @NotBlank(message = "计划标题不能为空")
    private String planTitle;

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    @NotBlank(message = "学期不能为空")
    private String semester;

    @NotBlank(message = "班级不能为空")
    private String className;
    private Long teacherId;
    private String teacherName;
    private String location;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /** 持续天数（后端根据 startDate/endDate 自动计算） */
    private Integer durationDays;
    private Integer totalHours;
    private Integer capacity;
    private String objective;
    private String content;
    private String assessment;
    private Integer status;

    /** 实训阶段明细，可随主表一起保存 */
    @Valid
    private List<TrainingPlanStageDTO> stages;
}
