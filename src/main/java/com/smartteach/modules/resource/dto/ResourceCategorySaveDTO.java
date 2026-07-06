package com.smartteach.modules.resource.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResourceCategorySaveDTO {
    private Long id;
    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    private String categoryCode;
    private Integer sort;
    private String icon;
    private String description;
    private Integer status;
}
