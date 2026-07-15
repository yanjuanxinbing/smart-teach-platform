package com.smartteach.modules.course.assignment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    /** 状态：0草稿 / 1已发布 / 2已截止 */
    private Integer status;

    /** 目标班级ID列表（非持久化字段，列表/详情/编辑回填用） */
    @TableField(exist = false)
    private List<Long> classIds;

    /** 所属章节标题（非持久化字段，列表展示用） */
    @TableField(exist = false)
    private String chapterTitle;
}
