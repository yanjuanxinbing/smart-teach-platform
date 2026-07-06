package com.smartteach.modules.experiment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 课程实验计划
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("experiment_plan")
public class ExperimentPlan extends BaseEntity {

    /** 计划标题 */
    private String planTitle;

    /** 关联课程ID */
    private Long courseId;

    /** 课程名称（冗余） */
    private String courseName;

    /** 学期 */
    private String semester;

    /** 班级 */
    private String className;

    /** 任课教师ID */
    private Long teacherId;

    /** 任课教师姓名 */
    private String teacherName;

    /** 实验地点（实验室/机房） */
    private String labRoom;

    /** 起始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 实验总次数 */
    private Integer totalExperiments;

    /** 实验总学时 */
    private Integer totalHours;

    /** 计划说明 */
    private String description;

    /** 状态 0草稿 1已发布 2已完成 3已驳回 */
    private Integer status;

    /** 审核人 */
    private Long approverId;

    private String approverName;

    private String approveRemark;
}
