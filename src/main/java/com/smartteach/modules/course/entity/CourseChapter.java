package com.smartteach.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程章节（一对多属于一门课程）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_chapter")
public class CourseChapter extends BaseEntity {

    /** 所属课程ID */
    private Long courseId;

    /** 父章节ID，顶级为0 */
    private Long parentId;

    /** 章节标题 */
    private String chapterTitle;

    /** 排序 */
    private Integer sort;

    /** 学时 */
    private Integer hours;
}
