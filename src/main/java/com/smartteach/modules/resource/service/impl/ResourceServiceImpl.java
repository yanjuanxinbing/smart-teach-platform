package com.smartteach.modules.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.resource.dto.ResourceQueryDTO;
import com.smartteach.modules.resource.dto.ResourceSaveDTO;
import com.smartteach.modules.resource.entity.Resource;
import com.smartteach.modules.resource.mapper.ResourceMapper;
import com.smartteach.modules.resource.service.ResourceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    @Override
    public PageResult<Resource> page(ResourceQueryDTO query) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getResourceName()), Resource::getResourceName, query.getResourceName())
                .eq(query.getResourceType() != null, Resource::getResourceType, query.getResourceType())
                .eq(query.getCategoryId() != null, Resource::getCategoryId, query.getCategoryId())
                .eq(query.getCourseId() != null, Resource::getCourseId, query.getCourseId())
                .like(StringUtils.isNotBlank(query.getTags()), Resource::getTags, query.getTags())
                .eq(query.getStatus() != null, Resource::getStatus, query.getStatus())
                .orderByDesc(Resource::getCreateTime);
        IPage<Resource> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public void save(ResourceSaveDTO dto) {
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        resource.setUploadBy(UserContext.getUserId());
        resource.setUploadName(UserContext.getUsername());
        if (resource.getStatus() == null) resource.setStatus(1);
        if (resource.getDownloadCount() == null) resource.setDownloadCount(0);
        if (resource.getViewCount() == null) resource.setViewCount(0);
        this.save(resource);
    }

    @Override
    public void update(ResourceSaveDTO dto) {
        Resource exists = this.getById(dto.getId());
        if (exists == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        this.updateById(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        Resource resource = new Resource();
        resource.setId(id);
        resource.setStatus(status);
        this.updateById(resource);
    }

    @Override
    public void incrementViewCount(Long id) {
        this.baseMapper.incrementViewCount(id);
    }

    @Override
    public void incrementDownloadCount(Long id) {
        this.baseMapper.incrementDownloadCount(id);
    }
}
