package com.smartteach.modules.training.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 实训计划
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("training_plan")
public class TrainingPlan extends BaseEntity {

    /** 计划标题 */
    private String planTitle;

    /** 实训项目名称 */
    private String projectName;

    /** 关联课程ID（可空） */
    private Long courseId;

    private String courseName;

    /** 学期 */
    private String semester;

    /** 班级 */
    private String className;

    /** 指导教师ID */
    private Long teacherId;

    private String teacherName;

    /** 实训地点 */
    private String location;

    /** 起始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 持续天数 */
    private Integer durationDays;

    /** 学时 */
    private Integer totalHours;

    /** 容纳人数 */
    private Integer capacity;

    /** 实训目标 */
    private String objective;

    /** 实训内容 */
    private String content;

    /** 考核方式 */
    private String assessment;

    /** 状态 0草稿 1已发布 2进行中 3已结束 4已驳回 */
    private Integer status;

    /** 审核人 */
    private Long approverId;
    private String approverName;
    private String approveRemark;
}
