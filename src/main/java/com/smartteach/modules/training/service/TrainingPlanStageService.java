package com.smartteach.modules.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.modules.training.entity.TrainingPlanStage;

import java.util.List;

public interface TrainingPlanStageService extends IService<TrainingPlanStage> {

    /** 根据计划ID查询所有阶段，按 sortNo 升序、startDate 升序 */
    List<TrainingPlanStage> listByPlanId(Long planId);
}
