package com.smartteach.modules.experiment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 实验分配记录 —— 老师/管理员在管理中心给班级/学生分配实验计划
 *
 * <p>状态：1已分配（默认）/ 3已完成</p>
 * <p>同一 (planId, studentId) 在 deleted=0 下唯一，重复分配会被去重</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("experiment_assignment")
public class ExperimentAssignment extends BaseEntity {

    private Long planId;
    private String planTitle;
    private Long studentId;
    private String studentName;
    private String className;
    private String phone;

    /** 状态 1已分配 3已完成 */
    private Integer status;

    private BigDecimal score;
    private String comment;
}