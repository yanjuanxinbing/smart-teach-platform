package com.smartteach.modules.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CourseChapterSaveDTO {
    private Long id;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    private Long parentId;

    @NotBlank(message = "章节标题不能为空")
    private String chapterTitle;

    private Integer sort;
    private Integer hours;
}
