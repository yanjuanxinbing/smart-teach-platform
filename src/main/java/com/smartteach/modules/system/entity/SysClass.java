package com.smartteach.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级（隶属 sys_dept）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_class")
public class SysClass extends BaseEntity {

    private String className;

    /** 年级（如 2022级） */
    private String grade;

    /** 所属部门ID */
    private Long deptId;

    private Integer sort;

    /** 0禁用 1启用 */
    private Integer status;
}