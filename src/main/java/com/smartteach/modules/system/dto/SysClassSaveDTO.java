package com.smartteach.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SysClassSaveDTO {

    private Long id;

    @NotBlank(message = "班级名称不能为空")
    private String className;

    /** 年级（如 2022级） */
    private String grade;

    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    private Integer sort;
    private Integer status;
}