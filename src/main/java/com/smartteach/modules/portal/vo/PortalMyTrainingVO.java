package com.smartteach.modules.portal.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 「我的实训」学生视图 —— 学生被分配的实训计划列表
 */
@Data
public class PortalMyTrainingVO {

    /** 实训报名记录ID（training_registration.id） */
    private Long registrationId;

    /** 实训计划ID（training_plan.id） */
    private Long trainingId;

    /** 计划名 */
    private String planName;

    /** 项目名 */
    private String projectName;

    /** 学期 */
    private String semester;

    /** 班级 */
    private String className;

    /** 指导教师 */
    private String teacherName;

    /** 起始 / 结束日期 */
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * 学生视角进度状态:
     *   not_started —— 未开始
     *   in_progress —— 进行中
     *   done        —— 已完成
     */
    private String status;

    /** 0-100 的具体进度（基于 start/end 与最终报名状态计算） */
    private Integer progress;
}
