package com.smartteach.modules.training.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TrainingPlanQueryDTO extends PageQuery {
    private String planTitle;
    private String projectName;
    private String semester;
    private String className;
    private Long teacherId;
    private Integer status;
}
