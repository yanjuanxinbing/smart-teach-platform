package com.smartteach.modules.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.course.dto.CoursePlanQueryDTO;
import com.smartteach.modules.course.dto.CoursePlanSaveDTO;
import com.smartteach.modules.course.entity.CoursePlan;
import com.smartteach.modules.course.vo.CoursePlanDetailVO;

import java.util.List;

public interface CoursePlanService extends IService<CoursePlan> {

    PageResult<CoursePlan> page(CoursePlanQueryDTO query);

    CoursePlanDetailVO detail(Long id);

    CoursePlan save(CoursePlanSaveDTO dto);

    void update(CoursePlanSaveDTO dto);

    void remove(List<Long> ids);

    /** 提交审核：草稿/驳回 → 待审核 */
    void submit(Long id);

    /** 审核通过：待审核 → 已发布 */
    void approve(Long id, Long approverId, String approverName, String remark);

    /** 审核驳回：待审核 → 驳回 */
    void reject(Long id, Long approverId, String approverName, String remark);

    /** 查询某教师创建的所有计划（用于"我的计划"） */
    List<CoursePlan> listByTeacher(Long teacherId);
}