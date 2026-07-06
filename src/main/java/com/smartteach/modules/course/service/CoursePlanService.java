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

    void save(CoursePlanSaveDTO dto);

    void update(CoursePlanSaveDTO dto);

    void remove(List<Long> ids);

    /** 提交审核 */
    void submit(Long id);

    /** 审核通过 */
    void approve(Long id, Long approverId, String approverName, String remark);

    /** 驳回 */
    void reject(Long id, Long approverId, String approverName, String remark);
}
