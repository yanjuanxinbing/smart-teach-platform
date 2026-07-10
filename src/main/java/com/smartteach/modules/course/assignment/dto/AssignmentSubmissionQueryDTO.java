package com.smartteach.modules.course.assignment.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教师批改端 / 学生本人 提交列表查询参数
 *
 * <p>学生端 studentId 由服务端注入，不在该 DTO 上接受客户端值。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssignmentSubmissionQueryDTO extends PageQuery {

    private Long assignmentId;
    private String studentName;
    private String className;
    private Integer status;
    private Integer isLate;
}
