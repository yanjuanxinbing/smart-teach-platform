package com.smartteach.modules.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.modules.resource.dto.ResourceCategorySaveDTO;
import com.smartteach.modules.resource.entity.ResourceCategory;
import com.smartteach.modules.resource.vo.ResourceCategoryTreeVO;

import java.util.List;

public interface ResourceCategoryService extends IService<ResourceCategory> {

    List<ResourceCategoryTreeVO> tree();

    void save(ResourceCategorySaveDTO dto);

    void update(ResourceCategorySaveDTO dto);

    void remove(Long id);
}
