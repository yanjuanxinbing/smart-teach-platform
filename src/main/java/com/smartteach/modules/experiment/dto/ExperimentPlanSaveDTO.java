package com.smartteach.modules.experiment.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
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

    /**
     * 结束日期必须晚于开始日期。
     * 字段级别的 @NotNull 仅校验"非空"，无法表达两个字段之间的关系，
     * 因此在校验链上补充一个类级别的 @AssertTrue 兜底，避免前端绕过。
     */
    @AssertTrue(message = "结束日期必须晚于开始日期")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}
