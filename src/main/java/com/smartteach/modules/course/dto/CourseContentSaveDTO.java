package com.smartteach.modules.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CourseContentSaveDTO {
    private Long id;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @NotBlank(message = "内容标题不能为空")
    private String contentTitle;

    @NotNull(message = "内容类型不能为空")
    private Integer contentType;

    private Long resourceId;
    private String resourceUrl;
    private String richText;
    private Integer sort;
    private Integer hours;
    private Integer status;
}
