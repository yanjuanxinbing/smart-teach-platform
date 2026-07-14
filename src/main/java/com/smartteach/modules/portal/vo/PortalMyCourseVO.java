package com.smartteach.modules.portal.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 「我的课程」学生视图 —— 已选课程列表
 */
@Data
public class PortalMyCourseVO {

    /** 课程ID（与 course.id 对应） */
    private Long courseId;

    private String courseName;
    private String courseCode;
    private String teacherName;
    private String coverImage;
    private Integer totalHours;

    /** 1必修 2选修 3通识 */
    private Integer courseType;

    /** 课程性质中文标签 */
    private String courseTypeLabel;

    /** 学习进度 0-100 */
    private Integer progress;

    private LocalDateTime enrolledAt;

    /** 选课状态 0已退课 1进行中 2已结课 */
    private Integer status;
}
