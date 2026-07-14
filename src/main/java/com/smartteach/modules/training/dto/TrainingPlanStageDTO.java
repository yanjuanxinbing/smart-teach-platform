package com.smartteach.modules.training.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * 实训计划阶段明细 DTO
 */
@Data
public class TrainingPlanStageDTO {
    private Long id;
    private Long planId;

    @NotBlank(message = "阶段名称不能为空")
    private String stageName;

    /** 阶段起始日期 */
    private LocalDate startDate;

    /** 阶段结束日期 */
    private LocalDate endDate;

    /** 持续天数（后端根据起止日期自动计算） */
    private Integer durationDays;

    /** 任务内容 */
    private String content;

    /** 备注 */
    private String remark;

    /** 排序号，越小越靠前 */
    private Integer sortNo;
}
