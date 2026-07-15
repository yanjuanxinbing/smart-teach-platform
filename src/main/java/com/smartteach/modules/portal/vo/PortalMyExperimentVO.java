package com.smartteach.modules.portal.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 「我的实验」学生视图 —— 单条实验明细（experiment_plan_item 的扩展视图）
 *
 * <p>学生只看到自己被分配的实验；每条记录对应一条 plan_item，由 item.classDate 与 today
 * 推算 status ∈ {not_started, in_progress, done}</p>
 */
@Data
public class PortalMyExperimentVO {

    /** 实验明细 ID —— experiment_plan_item.id */
    private Long experimentId;

    /** 所属实验计划 ID —— experiment_plan.id */
    private Long planId;

    /** 实验名称 */
    private String experimentName;

    /** 关联课程 */
    private Long courseId;
    private String courseName;

    /** 学期 / 班级 / 教师 / 实验室 */
    private String semester;
    private String className;
    private String teacherName;
    private String labRoom;

    /** 起止日期（来自 plan，item 级别只有 classDate） */
    private LocalDate startDate;
    private LocalDate endDate;

    /** 状态枚举：not_started / in_progress / done（基于 classDate 与 today 推算） */
    private String status;
}