package com.smartteach.modules.message.dto;

import lombok.Data;

import java.util.List;

/**
 * 标记已读 DTO
 */
@Data
public class MarkReadDTO {
    private List<Long> ids;
}
