package com.smartteach.common.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 实训计划状态枚举
 * 0草稿 / 1已发布 / 2审核中 / 3进行中 / 4已结束 / 5已驳回
 */
public enum TrainingPlanStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    REVIEWING(2, "审核中"),
    IN_PROGRESS(3, "进行中"),
    FINISHED(4, "已结束"),
    REJECTED(5, "驳回");

    private final int code;
    private final String label;

    TrainingPlanStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static TrainingPlanStatus of(Integer code) {
        if (code == null) return null;
        for (TrainingPlanStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }

    public static String labelOf(Integer code) {
        TrainingPlanStatus s = of(code);
        return s == null ? "-" : s.label;
    }

    /** Element Plus tag type 映射 */
    public static String tagType(Integer code) {
        TrainingPlanStatus s = of(code);
        if (s == null) return "info";
        switch (s) {
            case DRAFT: return "info";
            case PUBLISHED: return "primary";
            case REVIEWING: return "warning";
            case IN_PROGRESS: return "success";
            case FINISHED: return "success";
            case REJECTED: return "danger";
            default: return "info";
        }
    }

    public static List<TrainingPlanStatus> all() { return Arrays.asList(values()); }
}