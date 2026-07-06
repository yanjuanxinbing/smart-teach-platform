package com.smartteach.modules.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class UserSaveDTO {

    private Long id;

    @NotBlank(message = "登录账号不能为空")
    private String username;

    /** 新增时必填，编辑时留空表示不修改密码 */
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    private Integer gender;

    private Long deptId;

    private Integer status;

    private String remark;

    /** 分配的角色ID集合 */
    private List<Long> roleIds;
}
