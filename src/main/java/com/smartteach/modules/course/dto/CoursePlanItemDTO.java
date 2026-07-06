package com.smartteach.modules.course.dto;

import lombok.Data;

@Data
public class CoursePlanItemDTO {
    private Long id;
    private Long planId;
    private Integer weekNo;
    private Long chapterId;
    private String chapterTitle;
    private String content;
    private String objective;
    private String method;
    private Integer hours;
    private String remark;
}
