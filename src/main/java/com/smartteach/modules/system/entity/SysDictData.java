package com.smartteach.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典项
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    /** 关联字典类型编码 */
    private String dictType;

    /** 字典项标签 */
    private String dictLabel;

    /** 字典项值 */
    private String dictValue;

    /** CSS class */
    private String cssClass;

    /** 列表回显样式（default/primary/success/info/warning/danger） */
    private String listClass;

    private Integer sort;
    private Integer status;

    /** 是否默认 0否 1是 */
    private Integer isDefault;

    private String remark;
}
