package com.smartteach.modules.experiment.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExperimentPlanQueryDTO extends PageQuery {
    private String planTitle;
    private Long courseId;
    private String semester;
    private String className;
    private Long teacherId;
    private Integer status;
}
