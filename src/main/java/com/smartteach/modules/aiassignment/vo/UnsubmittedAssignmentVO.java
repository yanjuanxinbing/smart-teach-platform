package com.smartteach.modules.aiassignment.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 未提交 / 草稿 作业条目
 */
@Data
public class UnsubmittedAssignmentVO {

    private Long assignmentId;
    private String title;
    private LocalDateTime deadline;

    /** 0=草稿 1=已提交（出现在未提交列表里意味着有 draft 或无任何提交） */
    private Integer status;
}
