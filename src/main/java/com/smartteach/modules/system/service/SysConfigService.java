package com.smartteach.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.system.dto.SysConfigSaveDTO;
import com.smartteach.modules.system.entity.SysConfig;

import java.util.List;

public interface SysConfigService extends IService<SysConfig> {

    PageResult<SysConfig> page(PageQuery query);

    String getConfigByKey(String key);

    void save(SysConfigSaveDTO dto);

    void update(SysConfigSaveDTO dto);

    void remove(List<Long> ids);
}
