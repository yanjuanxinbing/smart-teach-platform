package com.smartteach.modules.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色名称 */
    private String roleName;

    /** 角色编码，如 ROLE_ADMIN，用于 Spring Security 鉴权 */
    private String roleCode;

    /** 排序 */
    private Integer sort;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
