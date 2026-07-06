package com.smartteach.modules.permission.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class RoleSaveDTO {
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private Integer sort;
    private Integer status;
    private String remark;

    /** 分配的菜单/按钮权限ID集合 */
    private List<Long> menuIds;
}
