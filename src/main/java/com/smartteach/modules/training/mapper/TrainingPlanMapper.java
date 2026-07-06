package com.smartteach.modules.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteach.modules.training.entity.TrainingPlan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainingPlanMapper extends BaseMapper<TrainingPlan> {
}
