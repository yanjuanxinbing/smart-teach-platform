package com.smartteach.modules.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 部门下拉专用轻量 VO（不含 leader / phone / email 等 PII），
 * 给作业表单之类任意登录用户都可读的"参考数据"接口使用。
 */
@Data
public class SysDeptOptionVO {
    private Long id;
    private Long parentId;
    private String deptName;
    private List<SysDeptOptionVO> children;
}