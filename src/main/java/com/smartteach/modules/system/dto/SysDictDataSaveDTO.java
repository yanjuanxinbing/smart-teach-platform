package com.smartteach.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SysDictDataSaveDTO {
    private Long id;

    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    private String cssClass;
    private String listClass;
    private Integer sort;
    private Integer status;
    private Integer isDefault;
    private String remark;
}
