package com.smartteach.modules.resource.vo;

import lombok.Data;

import java.util.List;

@Data
public class ResourceCategoryTreeVO {
    private Long id;
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private Integer sort;
    private String icon;
    private String description;
    private Integer status;
    private List<ResourceCategoryTreeVO> children;
}
