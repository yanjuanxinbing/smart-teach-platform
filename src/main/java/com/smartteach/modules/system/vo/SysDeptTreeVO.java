package com.smartteach.modules.system.vo;

import lombok.Data;

import java.util.List;

@Data
public class SysDeptTreeVO {
    private Long id;
    private Long parentId;
    private String deptName;
    private String deptCode;
    private Integer sort;
    private String leader;
    private String phone;
    private String email;
    private Integer status;
    private List<SysDeptTreeVO> children;
}
