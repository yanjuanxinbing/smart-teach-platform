package com.smartteach.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.system.dto.SysDictTypeSaveDTO;
import com.smartteach.modules.system.entity.SysDictType;

import java.util.List;

public interface SysDictTypeService extends IService<SysDictType> {

    PageResult<SysDictType> page(PageQuery query);

    void save(SysDictTypeSaveDTO dto);

    void update(SysDictTypeSaveDTO dto);

    void remove(List<Long> ids);

    List<SysDictType> listAllEnabled();
}
