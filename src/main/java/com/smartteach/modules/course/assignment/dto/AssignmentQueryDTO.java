package com.smartteach.modules.course.assignment.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教师管理作业列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssignmentQueryDTO extends PageQuery {

    private Long courseId;
    private Long chapterId;
    private Long contentId;

    /** 模糊匹配标题 */
    private String keyword;

    private Integer status;

    /** 学生端我的作业：排除草稿（status=0）作业 */
    private Boolean excludeDraft;
}
