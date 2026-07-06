package com.smartteach.modules.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单/权限项（目录、菜单、按钮三级）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 父级ID，顶级为0 */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 类型 1目录 2菜单 3按钮 */
    private Integer menuType;

    /** 路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 图标 */
    private String icon;

    /** 权限标识，如 user:add，按钮类型必填，供后端接口鉴权 */
    private String permission;

    /** 排序 */
    private Integer sort;

    /** 是否显示 0隐藏 1显示 */
    private Integer visible;

    /** 状态 0禁用 1启用 */
    private Integer status;
}
