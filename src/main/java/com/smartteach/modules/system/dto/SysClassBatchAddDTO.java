package com.smartteach.modules.system.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量绑定班级成员（append-only，不替换现有成员）
 */
@Data
public class SysClassBatchAddDTO {

    @NotEmpty(message = "至少要有一行数据")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotBlank(message = "班级名称不能为空")
        private String className;

        @NotBlank(message = "账号不能为空")
        private String username;
    }
}
