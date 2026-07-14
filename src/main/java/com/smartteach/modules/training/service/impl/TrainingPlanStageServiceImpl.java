package com.smartteach.modules.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.modules.training.entity.TrainingPlanStage;
import com.smartteach.modules.training.mapper.TrainingPlanStageMapper;
import com.smartteach.modules.training.service.TrainingPlanStageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingPlanStageServiceImpl extends ServiceImpl<TrainingPlanStageMapper, TrainingPlanStage>
        implements TrainingPlanStageService {

    @Override
    public List<TrainingPlanStage> listByPlanId(Long planId) {
        return this.list(new LambdaQueryWrapper<TrainingPlanStage>()
                .eq(TrainingPlanStage::getPlanId, planId)
                .orderByAsc(TrainingPlanStage::getSortNo)
                .orderByAsc(TrainingPlanStage::getStartDate));
    }
}
