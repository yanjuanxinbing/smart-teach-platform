package com.smartteach.modules.experiment.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExperimentPlanItemDTO {
    private Long id;
    private Long planId;
    private Integer expNo;
    private String expName;
    private Integer expType;
    private String purpose;
    private String content;
    private String requirement;
    private Long resourceId;
    private LocalDate classDate;
    private String classPeriod;
    private Integer hours;
    private String teacherName;
    private String remark;
}
