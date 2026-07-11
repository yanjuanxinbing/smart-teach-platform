package com.smartteach.modules.course.assignment.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业发布/编辑表单 DTO（同一份 DTO 同时被新增和编辑复用）
 */
@Data
public class AssignmentSaveDTO {

    /** 编辑时携带；新增时为 null */
    private Long id;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    /** 关联章节内容（可选；不填表示挂在整个章节下） */
    private Long contentId;

    @NotBlank(message = "作业标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;

    @DecimalMin(value = "0.00", message = "总分不能小于 0")
    @DecimalMax(value = "100.00", message = "总分不能大于 100")
    private BigDecimal totalScore;

    /** 0 不允许迟交 / 1 允许迟交；默认 1 */
    private Integer allowLate;

    /** 状态：0草稿 / 1已发布 / 2已截止 */
    private Integer status;
}
