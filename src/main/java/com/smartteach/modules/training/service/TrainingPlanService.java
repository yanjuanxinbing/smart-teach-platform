package com.smartteach.modules.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.training.dto.TrainingPlanQueryDTO;
import com.smartteach.modules.training.dto.TrainingPlanSaveDTO;
import com.smartteach.modules.training.entity.TrainingPlan;

import java.util.List;

public interface TrainingPlanService extends IService<TrainingPlan> {

    PageResult<TrainingPlan> page(TrainingPlanQueryDTO query);

    TrainingPlan detail(Long id);

    TrainingPlan save(TrainingPlanSaveDTO dto);

    void update(TrainingPlanSaveDTO dto);

    void remove(List<Long> ids);

    void publish(Long id);

    /**
     * 审核通过：已发布(1) 或历史遗留的审核中(2) → 进行中(3)
     */
    void approve(Long id, Long approverId, String approverName, String remark);

    /**
     * 审核驳回：已发布(1) 或历史遗留的审核中(2) → 已驳回(5)
     */
    void reject(Long id, Long approverId, String approverName, String remark);

    void finish(Long id);

    void revertToDraft(Long id);

    /**
     * 获取已存在的班级列表（去重、空值过滤），用于报名管理等场景的下拉选择
     */
    List<String> listDistinctClasses();
}