package com.smartteach.modules.portal.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 门户-实训计划视图 —— 既用于「可报名列表」也用于「计划详情」
 *
 * <p>学生可浏览所有"进行中"(status=3)的计划；当前学生是否已报名由
 * {@link #registered} 与 {@link #registrationStatus} 表达，避免前端再发一次详情请求。</p>
 */
@Data
public class PortalTrainingVO {

    /** 计划ID */
    private Long planId;

    /** 计划标题 */
    private String planTitle;

    /** 实训项目名 */
    private String projectName;

    /** 学期 */
    private String semester;

    /** 班级 */
    private String className;

    /** 指导教师 */
    private String teacherName;

    /** 实训地点 */
    private String location;

    /** 起止 */
    private LocalDate startDate;
    private LocalDate endDate;

    /** 计划状态 0草稿 1已发布 2审核中 3进行中 4已结束 5已驳回 */
    private Integer status;

    /** 容纳人数（可能为 null） */
    private Integer capacity;

    /** 已报名人数（status IN 0,1,2 的记录数），用于前端判断余位 */
    private Integer registeredCount;

    /** 实训目标 */
    private String objective;
    /** 实训内容 */
    private String content;
    /** 考核方式 */
    private String assessment;

    // === 学生当前视角 ===
    /** 当前学生是否已经报名过该计划（含任意状态：待审核/已通过/已驳回/已完成） */
    private Boolean registered;

    /** 报名记录 ID（已报名才有） */
    private Long registrationId;

    /** 报名状态 0待审核 1已通过 2已驳回 3已完成（已报名才有） */
    private Integer registrationStatus;
}