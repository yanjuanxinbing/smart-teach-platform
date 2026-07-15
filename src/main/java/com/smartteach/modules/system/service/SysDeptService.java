package com.smartteach.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.modules.system.dto.SysDeptSaveDTO;
import com.smartteach.modules.system.entity.SysDept;
import com.smartteach.modules.system.vo.SysDeptOptionVO;
import com.smartteach.modules.system.vo.SysDeptTreeVO;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    /** 完整部门树（含 PII），用于部门管理页面 */
    List<SysDeptTreeVO> tree();

    /**
     * 部门树下拉专用（不含 PII），给作业表单等参考数据接口使用。
     * 任何登录用户都可读。
     */
    List<SysDeptOptionVO> treeOptions();

    void save(SysDeptSaveDTO dto);

    void update(SysDeptSaveDTO dto);

    void remove(Long id);
}
