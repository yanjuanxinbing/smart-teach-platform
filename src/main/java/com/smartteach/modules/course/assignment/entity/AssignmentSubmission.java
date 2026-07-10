package com.smartteach.modules.course.assignment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业提交（学生对作业的一次提交/一次重新提交各产生一行）
 *
 * <p>状态：0草稿 / 1已提交 / 2已批改</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assignment_submission")
public class AssignmentSubmission extends BaseEntity {

    private Long assignmentId;
    private Long studentId;
    private String studentName;
    private String className;

    /** 提交正文 */
    private String submitText;

    /** 附件 URL（/api/files/yyyy/MM/xxx.ext） */
    private String fileUrl;
    private String originalName;
    private String fileSuffix;
    private Long fileSize;

    /** 是否迟交：0否 1是 */
    private Integer isLate;

    /** 学生正式提交时间（草稿时为 null） */
    private LocalDateTime submitTime;

    /** 教师评分（0–100，可空） */
    private BigDecimal score;

    /** 教师评语 */
    private String comment;

    private Long graderId;
    private String graderName;
    private LocalDateTime gradeTime;

    /** 状态：0草稿 / 1已提交 / 2已批改 */
    private Integer status;
}
