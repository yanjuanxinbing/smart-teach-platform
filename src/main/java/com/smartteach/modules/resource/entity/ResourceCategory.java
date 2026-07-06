package com.smartteach.modules.resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resource_category")
public class ResourceCategory extends BaseEntity {

    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private Integer sort;
    private String icon;
    private String description;
    private Integer status;
}
