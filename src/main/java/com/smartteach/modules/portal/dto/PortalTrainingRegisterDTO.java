package com.smartteach.modules.portal.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 学生门户自报名请求 —— 仅携带 planId
 * <p>studentId 严格从 JWT 取 (UserContext.getUserId())，不接受请求体里的同名字段，避免 IDOR</p>
 */
@Data
public class PortalTrainingRegisterDTO {

    @NotNull(message = "实训计划ID不能为空")
    private Long planId;
}