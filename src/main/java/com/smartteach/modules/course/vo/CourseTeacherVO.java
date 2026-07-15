package com.smartteach.modules.course.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 授课管理列表 / 详情 VO。
 * 一行对应一个 course_teacher 关系，关联展示课程和教师的可读名称。
 */
@Data
public class CourseTeacherVO {

    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String role;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}