package com.smartteach.modules.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级视图对象（含 deptName / memberCount 联表信息）
 */
@Data
public class SysClassVO {

    private Long id;
    private String className;
    private String grade;
    private Long deptId;
    private String deptName;
    private Integer sort;
    private Integer status;
    private Integer memberCount;
    private LocalDateTime createTime;
}