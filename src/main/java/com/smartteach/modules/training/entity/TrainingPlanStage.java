package com.smartteach.modules.training.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 实训计划阶段明细
 * 描述一个实训计划被拆分成几个阶段，每个阶段有自己的起止日期、任务内容等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("training_plan_stage")
public class TrainingPlanStage extends BaseEntity {

    /** 所属计划ID */
    private Long planId;

    /** 阶段名称 */
    private String stageName;

    /** 阶段起始日期 */
    private LocalDate startDate;

    /** 阶段结束日期 */
    private LocalDate endDate;

    /** 持续天数 */
    private Integer durationDays;

    /** 任务内容 */
    private String content;

    /** 备注 */
    private String remark;

    /** 排序号 */
    private Integer sortNo;
}
