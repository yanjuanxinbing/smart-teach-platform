package com.smartteach.modules.profile.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 改密 DTO（前端 portal / admin ProfileSecurity 共用）
 */
@Data
public class ChangeOwnPasswordDTO {

    @NotBlank(message = "请输入原密码")
    private String oldPassword;

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 32, message = "密码长度 6-32 位")
    private String newPassword;
}
