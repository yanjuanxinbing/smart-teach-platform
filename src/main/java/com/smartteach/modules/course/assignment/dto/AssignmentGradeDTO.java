package com.smartteach.modules.course.assignment.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class AssignmentGradeDTO {

    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0", message = "成绩不能小于 0")
    @DecimalMax(value = "100", message = "成绩不能大于 100")
    private BigDecimal score;

    @Size(max = 500, message = "评语最多 500 字")
    private String comment;
}
