package com.smartteach.modules.training.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TrainingRegistrationSaveDTO {
    private Long id;

    @NotNull(message = "实训计划不能为空")
    private Long planId;

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    private String studentName;
    private String className;
    private String phone;
    private Integer status;

    private BigDecimal score;
    private String comment;
}
