package com.smartteach.modules.resource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.modules.resource.dto.ResourceCategorySaveDTO;
import com.smartteach.modules.resource.entity.ResourceCategory;
import com.smartteach.modules.resource.mapper.ResourceCategoryMapper;
import com.smartteach.modules.resource.service.ResourceCategoryService;
import com.smartteach.modules.resource.vo.ResourceCategoryTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ResourceCategoryServiceImpl extends ServiceImpl<ResourceCategoryMapper, ResourceCategory>
        implements ResourceCategoryService {

    @Override
    public List<ResourceCategoryTreeVO> tree() {
        List<ResourceCategory> all = this.lambdaQuery().orderByAsc(ResourceCategory::getSort).list();
        return buildTree(all, 0L);
    }

    @Override
    public void save(ResourceCategorySaveDTO dto) {
        ResourceCategory category = new ResourceCategory();
        BeanUtils.copyProperties(dto, category);
        if (category.getParentId() == null) category.setParentId(0L);
        if (category.getStatus() == null) category.setStatus(1);
        this.save(category);
    }

    @Override
    public void update(ResourceCategorySaveDTO dto) {
        ResourceCategory category = new ResourceCategory();
        BeanUtils.copyProperties(dto, category);
        this.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        boolean hasChildren = this.lambdaQuery().eq(ResourceCategory::getParentId, id).exists();
        if (hasChildren) {
            throw new BusinessException("请先删除子分类");
        }
        this.removeById(id);
    }

    private List<ResourceCategoryTreeVO> buildTree(List<ResourceCategory> all, Long parentId) {
        List<ResourceCategoryTreeVO> result = new ArrayList<>();
        for (ResourceCategory c : all) {
            if (c.getParentId().equals(parentId)) {
                ResourceCategoryTreeVO vo = new ResourceCategoryTreeVO();
                BeanUtils.copyProperties(c, vo);
                vo.setChildren(buildTree(all, c.getId()));
                result.add(vo);
            }
        }
        result.sort(Comparator.comparing(o -> o.getSort() == null ? 0 : o.getSort()));
        return result;
    }
}
