package com.smartteach.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.modules.system.dto.SysDeptSaveDTO;
import com.smartteach.modules.system.entity.SysDept;
import com.smartteach.modules.system.vo.SysDeptTreeVO;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    List<SysDeptTreeVO> tree();

    void save(SysDeptSaveDTO dto);

    void update(SysDeptSaveDTO dto);

    void remove(Long id);
}
