package com.smartteach.modules.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.resource.dto.ResourceQueryDTO;
import com.smartteach.modules.resource.dto.ResourceSaveDTO;
import com.smartteach.modules.resource.entity.Resource;

import java.util.List;

public interface ResourceService extends IService<Resource> {

    PageResult<Resource> page(ResourceQueryDTO query);

    void save(ResourceSaveDTO dto);

    void update(ResourceSaveDTO dto);

    void remove(List<Long> ids);

    void changeStatus(Long id, Integer status);

    /** 浏览/下载时调用 */
    void incrementViewCount(Long id);

    void incrementDownloadCount(Long id);
}
