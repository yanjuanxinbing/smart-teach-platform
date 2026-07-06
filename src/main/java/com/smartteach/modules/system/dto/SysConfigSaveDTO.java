package com.smartteach.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SysConfigSaveDTO {
    private Long id;

    @NotBlank(message = "参数名称不能为空")
    private String configName;

    @NotBlank(message = "参数键名不能为空")
    private String configKey;

    private String configValue;
    private Integer configType;
    private String remark;
}
