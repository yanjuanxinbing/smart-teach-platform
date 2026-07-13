package com.smartteach.modules.portal.dto;

import lombok.Data;

/**
 * 前台门户 - 代码库查询条件（公开）
 */
@Data
public class PortalCodexQueryDTO {
    private Long current = 1L;
    private Long size = 12L;

    /** 语言标识：java / python / js ... */
    private String lang;
    /** 标题/标签/作者模糊搜索 */
    private String q;
}
