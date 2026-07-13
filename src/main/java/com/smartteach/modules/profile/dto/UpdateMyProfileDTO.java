package com.smartteach.modules.profile.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

/**
 * 我的资料 - 个人更新 DTO
 */
@Data
public class UpdateMyProfileDTO {

    @Size(max = 50, message = "姓名长度不超过 50")
    private String realName;

    @Size(max = 50, message = "昵称长度不超过 50")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;

    @Size(max = 200, message = "个人简介过长")
    private String bio;
}
