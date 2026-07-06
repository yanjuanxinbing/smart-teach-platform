package com.smartteach.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 课程计划（教学进度计划）：
 * 一份课程计划按周次分解教学任务，绑定到一门课程、一个学期/班级。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_plan")
public class CoursePlan extends BaseEntity {

    /** 计划标题 */
    private String planTitle;

    /** 关联课程ID */
    private Long courseId;

    /** 课程名称（冗余） */
    private String courseName;

    /** 学期，如 2024-2025-1 */
    private String semester;

    /** 班级 */
    private String className;

    /** 起始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 总周次 */
    private Integer totalWeeks;

    /** 计划说明 */
    private String description;

    /** 状态 0草稿 1已发布 2已完成 */
    private Integer status;

    /** 审核人ID */
    private Long approverId;

    /** 审核人姓名 */
    private String approverName;

    /** 审核意见 */
    private String approveRemark;
}
