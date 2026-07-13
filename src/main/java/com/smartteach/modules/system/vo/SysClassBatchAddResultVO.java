package com.smartteach.modules.system.vo;

import lombok.Data;

/**
 * 批量绑定班级成员的结果
 */
@Data
public class SysClassBatchAddResultVO {

    /** 总条数 */
    private Integer total;

    /** 成功新绑定的条数 */
    private Integer success;

    /** 已是该班级成员、跳过 */
    private Integer skipped;

    /** 失败的条数 */
    private Integer failed;

    /** 失败明细（不会超过 total 条） */
    private java.util.List<FailItem> errors;

    @Data
    public static class FailItem {
        private String className;
        private String username;
        private String reason;
    }
}
