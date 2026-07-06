package com.smartteach.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.base.PageQuery;
import com.smartteach.modules.system.dto.SysDictDataSaveDTO;
import com.smartteach.modules.system.entity.SysDictData;

import java.util.List;

public interface SysDictDataService extends IService<SysDictData> {

    PageResult<SysDictData> page(String dictType, PageQuery query);

    List<SysDictData> listByType(String dictType);

    void save(SysDictDataSaveDTO dto);

    void update(SysDictDataSaveDTO dto);

    void remove(List<Long> ids);
}
