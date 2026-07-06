package com.smartteach.modules.permission.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuTreeVO {
    private Long id;
    private Long parentId;
    private String menuName;
    private Integer menuType;
    private String path;
    private String component;
    private String icon;
    private String permission;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private List<MenuTreeVO> children;
}
