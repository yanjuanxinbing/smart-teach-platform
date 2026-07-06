package com.smartteach.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 课程信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    /** 课程编号 */
    private String courseCode;

    /** 课程名称 */
    private String courseName;

    /** 课程分类ID */
    private Long categoryId;

    /** 课程分类名称（冗余字段，方便列表展示） */
    private String categoryName;

    /** 课程简介 */
    private String description;

    /** 封面图 */
    private String coverImage;

    /** 任课教师ID */
    private Long teacherId;

    /** 任课教师姓名 */
    private String teacherName;

    /** 学分 */
    private BigDecimal credit;

    /** 总学时 */
    private Integer totalHours;

    /** 课程性质 1必修 2选修 3通识 */
    private Integer courseType;

    /** 状态 0未发布 1已发布 2已结课 */
    private Integer status;
}
