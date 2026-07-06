package com.smartteach.modules.training.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 实训报名记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("training_registration")
public class TrainingRegistration extends BaseEntity {

    private Long planId;
    private String planTitle;
    private Long studentId;
    private String studentName;
    private String className;
    private String phone;

    /** 报名状态 0待审核 1已通过 2已驳回 3已完成 */
    private Integer status;

    /** 签到时间 */
    private LocalDateTime signInTime;

    /** 签退时间 */
    private LocalDateTime signOutTime;

    /** 成绩 */
    private java.math.BigDecimal score;

    /** 评语 */
    private String comment;
}
