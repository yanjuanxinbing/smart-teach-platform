package com.smartteach.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SysDeptSaveDTO {
    private Long id;
    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private String deptCode;
    private Integer sort;
    private String leader;
    private String phone;
    private String email;
    private Integer status;
}
