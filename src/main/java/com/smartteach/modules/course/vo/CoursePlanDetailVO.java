package com.smartteach.modules.course.vo;

import com.smartteach.modules.course.entity.CoursePlan;
import com.smartteach.modules.course.entity.CoursePlanItem;
import lombok.Data;

import java.util.List;

@Data
public class CoursePlanDetailVO {
    private CoursePlan plan;
    private List<CoursePlanItem> items;
}
