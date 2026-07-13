package com.smartteach.modules.auth.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 自助注册表单 DTO（教师/学生）
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "工号/学号不能为空")
    @Size(min = 3, max = 30, message = "账号长度 3-30 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "手机号必须是 11 位数字")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 角色 code（硬白名单：ROLE_TEACHER / ROLE_STUDENT） */
    @NotBlank(message = "请选择角色")
    @Pattern(regexp = "^ROLE_(TEACHER|STUDENT)$", message = "角色仅限教师或学生")
    private String roleCode;
}
