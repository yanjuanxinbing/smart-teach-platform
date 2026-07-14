package com.smartteach.modules.portal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 学生-课程 选课关系（用于门户侧"我的课程"）
 *
 * <p>安全约束：student_id 来自 UserContext.getUserId()，不要从请求参数中读取</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_enrollment")
public class CourseEnrollment extends BaseEntity {

    private Long studentId;
    private Long courseId;

    /** 冗余字段，列表展示用（避免 N+1 关联） */
    private String courseName;
    private String courseCode;
    private String teacherName;
    private String coverImage;
    private Integer totalHours;

    /** 冗余 1必修 2选修 3通识 */
    private Integer courseType;

    /** 学习进度 0-100 */
    private Integer progress;

    /** 选课时间 */
    private LocalDateTime enrolledAt;

    /** 0已退课 1进行中 2已结课 */
    private Integer status;
}
