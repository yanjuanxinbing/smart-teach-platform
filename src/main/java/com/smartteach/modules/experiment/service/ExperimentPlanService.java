package com.smartteach.modules.experiment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.experiment.dto.ExperimentPlanQueryDTO;
import com.smartteach.modules.experiment.dto.ExperimentPlanSaveDTO;
import com.smartteach.modules.experiment.entity.ExperimentPlan;
import com.smartteach.modules.experiment.vo.ExperimentPlanDetailVO;

import java.util.List;

public interface ExperimentPlanService extends IService<ExperimentPlan> {

    PageResult<ExperimentPlan> page(ExperimentPlanQueryDTO query);

    ExperimentPlanDetailVO detail(Long id);

    ExperimentPlan save(ExperimentPlanSaveDTO dto);

    void update(ExperimentPlanSaveDTO dto);

    void remove(List<Long> ids);

    void submit(Long id);

    void approve(Long id, Long approverId, String approverName, String remark);

    void reject(Long id, Long approverId, String approverName, String remark);

    /** 实验计划中出现的班级（去重），用于"实验评分"班级筛选下拉 */
    List<String> listDistinctClasses();
}
