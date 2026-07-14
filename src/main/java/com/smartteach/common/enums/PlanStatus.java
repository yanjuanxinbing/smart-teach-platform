package com.smartteach.common.enums;

import lombok.Getter;

@Getter
public enum PlanStatus {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    REJECTED(3, "驳回"),
    PENDING(4, "待审核");

    private final Integer code;
    private final String label;

    PlanStatus(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(Integer code) {
        if (code == null) {
            return "未知";
        }
        for (PlanStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getLabel();
            }
        }
        return "未知";
    }
}