package com.smartteach.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SysDictTypeSaveDTO {
    private Long id;

    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    private String description;
    private Integer status;
}
