package com.smartteach.modules.course.assignment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.course.assignment.dto.AssignmentSubmissionQueryDTO;
import com.smartteach.modules.course.assignment.dto.AssignmentSubmissionSaveDTO;
import com.smartteach.modules.course.assignment.entity.AssignmentSubmission;

import java.math.BigDecimal;
import java.util.List;

public interface AssignmentSubmissionService extends IService<AssignmentSubmission> {

    /** 教师按条件查询（按 submit_time DESC 排序） */
    PageResult<AssignmentSubmission> page(AssignmentSubmissionQueryDTO query);

    /** 学生本人的提交列表（studentId 由服务端注入） */
    PageResult<AssignmentSubmission> myPage(AssignmentSubmissionQueryDTO query);

    /** 学生对指定作业的"最近一次提交"快照（用于提交页初始化） */
    AssignmentSubmission latest(Long assignmentId, Long studentId);

    /** 学生保存草稿（status=0）；upsert 同 (assignmentId, studentId, status=0) */
    Long saveDraft(AssignmentSubmissionSaveDTO dto);

    /** 学生正式提交（status 0→1，写入 submit_time 与 is_late） */
    void submit(AssignmentSubmissionSaveDTO dto);

    /** 教师批改打分（status 1→2） */
    void grade(Long id, BigDecimal score, String comment);

    void remove(List<Long> ids);
}
