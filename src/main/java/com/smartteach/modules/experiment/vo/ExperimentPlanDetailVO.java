package com.smartteach.modules.experiment.vo;

import com.smartteach.modules.experiment.entity.ExperimentPlan;
import com.smartteach.modules.experiment.entity.ExperimentPlanItem;
import lombok.Data;

import java.util.List;

@Data
public class ExperimentPlanDetailVO {
    private ExperimentPlan plan;
    private List<ExperimentPlanItem> items;
}
