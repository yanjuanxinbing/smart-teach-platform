package com.smartteach.modules.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码（如启用图形验证码校验） */
    private String captcha;

    private String captchaKey;
}
