package com.smartteach.modules.training.vo;

import com.smartteach.modules.training.entity.TrainingPlan;
import com.smartteach.modules.training.entity.TrainingPlanStage;
import lombok.Data;

import java.util.List;

@Data
public class TrainingPlanDetailVO {
    private TrainingPlan plan;
    private List<TrainingPlanStage> stages;
}
