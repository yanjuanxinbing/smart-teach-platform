package com.smartteach.modules.course.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 授课分配 DTO：对一门课程全量替换教师集合（先删后插模式）。
 * 入参 teachers 不能为空（至少一条主讲）；空集合不允许——意味着彻底解除该课程全部授课关系。
 */
@Data
public class CourseTeacherAssignDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotEmpty(message = "教师不能为空")
    private List<TeacherItem> teachers;

    @Data
    public static class TeacherItem {
        @NotNull(message = "教师ID不能为空")
        private Long teacherId;
        /** 主讲 / 助教 */
        private String role;
        private Integer sort;
    }
}