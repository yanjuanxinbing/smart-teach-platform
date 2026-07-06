package com.smartteach.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程计划明细：按周次拆分具体教学内容
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_plan_item")
public class CoursePlanItem extends BaseEntity {

    /** 所属计划ID */
    private Long planId;

    /** 周次 */
    private Integer weekNo;

    /** 章节ID（关联 course_chapter） */
    private Long chapterId;

    /** 章节标题（冗余） */
    private String chapterTitle;

    /** 教学内容 */
    private String content;

    /** 教学目标 */
    private String objective;

    /** 教学方法 */
    private String method;

    /** 学时 */
    private Integer hours;

    /** 备注 */
    private String remark;
}
