package com.smartteach.modules.course.assignment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业（教师在章节内容下发布）
 *
 * <p>状态：0草稿 / 1已发布 / 2已截止</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assignment")
public class Assignment extends BaseEntity {

    private Long courseId;
    private Long chapterId;
    private Long contentId;
    private String title;
    private String description;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 总分（默认 100） */
    private BigDecimal totalScore;

    /** 是否允许迟交：0否 1是 */
    private Integer allowLate;

    /** 状态：0草稿 / 1已发布 / 2已截止 */
    private Integer status;
}
