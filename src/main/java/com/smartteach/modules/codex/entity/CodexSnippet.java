package com.smartteach.modules.codex.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码库（CodeX）片段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("codex_snippet")
public class CodexSnippet extends BaseEntity {

    /** 片段标题 */
    private String title;

    /** 语言（java/python/js/...） */
    private String language;

    /** 完整代码（可能很长） */
    private String code;

    /** 列表中展示的预览（前 ~240 字） */
    private String preview;

    /** 详细说明 */
    private String description;

    /** 逗号分隔的标签 */
    private String tags;

    /** 作者显示名 */
    private String author;

    /** 浏览次数 */
    private Integer views;

    /** 0 仅自己可见 / 1 公开 */
    private Integer isPublic;
}
