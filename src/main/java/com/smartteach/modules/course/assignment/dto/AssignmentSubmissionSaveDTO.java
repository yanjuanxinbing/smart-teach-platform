package com.smartteach.modules.course.assignment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 学生提交作业 DTO（同时承载"保存草稿"和"正式提交"两路入口）
 *
 * <p>student_id / student_name / class_name / is_late / submit_time 全部由服务端
 * 基于 UserContext 与作业 deadline 计算后写入，不允许客户端伪造。</p>
 */
@Data
public class AssignmentSubmissionSaveDTO {

    /** 编辑已存在的草稿时携带；新增草稿 / 提交时为 null（生成新行） */
    private Long id;

    @NotNull(message = "作业ID不能为空")
    private Long assignmentId;

    /** 提交正文（可空，配合文件型提交） */
    private String submitText;

    /** 文件四件套，由前端调用 /biz/resource/upload 上传后填入 */
    private String fileUrl;
    private String originalName;
    private String fileSuffix;
    private Long fileSize;
}
