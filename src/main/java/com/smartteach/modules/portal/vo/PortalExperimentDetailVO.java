package com.smartteach.modules.portal.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 「实验详情」学生视图 —— 单条实验明细的完整字段
 */
@Data
public class PortalExperimentDetailVO {

    // === item 字段 ===
    private Long itemId;
    private Long planId;
    private Integer expNo;
    private String expName;
    /** 实验类型 1验证 2综合 3设计 4创新 */
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

    // === plan 冗余字段 ===
    private String planTitle;
    private Long courseId;
    private String courseName;
    private String semester;
    private String className;
    private String labRoom;
    private LocalDate planStartDate;
    private LocalDate planEndDate;

    // === 学生当前状态 ===
    /** 当前学生是否被分配该实验（即 experiment_assignment 中存在记录） */
    private Boolean assigned;

    /** 分配状态 1已分配 3已完成 */
    private Integer assignmentStatus;

    /** 实验成绩（仅已完成时有） */
    private BigDecimal score;

    /** 评语 */
    private String comment;

    /** item 状态：由 classDate vs today 推算 */
    private String status;
}