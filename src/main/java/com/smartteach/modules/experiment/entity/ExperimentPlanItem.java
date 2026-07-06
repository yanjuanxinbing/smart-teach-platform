package com.smartteach.modules.experiment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 实验计划明细（单次实验安排）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("experiment_plan_item")
public class ExperimentPlanItem extends BaseEntity {

    private Long planId;

    /** 实验序号 */
    private Integer expNo;

    /** 实验名称 */
    private String expName;

    /** 实验类型 1验证 2综合 3设计 4创新 */
    private Integer expType;

    /** 实验目的 */
    private String purpose;

    /** 实验内容 */
    private String content;

    /** 实验要求 */
    private String requirement;

    /** 关联资源ID（实验指导书等） */
    private Long resourceId;

    /** 上课日期 */
    private LocalDate classDate;

    /** 节次，如 1-2节 */
    private String classPeriod;

    /** 学时 */
    private Integer hours;

    /** 任课教师 */
    private String teacherName;

    /** 备注 */
    private String remark;
}
