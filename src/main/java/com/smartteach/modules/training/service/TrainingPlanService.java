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

    void save(TrainingPlanSaveDTO dto);

    void update(TrainingPlanSaveDTO dto);

    void remove(List<Long> ids);

    void publish(Long id);

    void finish(Long id);

    void approve(Long id, Long approverId, String approverName, String remark);

    void reject(Long id, Long approverId, String approverName, String remark);
}
