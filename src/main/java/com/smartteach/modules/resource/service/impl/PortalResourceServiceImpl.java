package com.smartteach.modules.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.modules.resource.dto.ResourceQueryDTO;
import com.smartteach.modules.resource.entity.Resource;
import com.smartteach.modules.resource.mapper.ResourceMapper;
import com.smartteach.modules.resource.service.PortalResourceService;
import com.smartteach.modules.resource.vo.PortalResourceVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 门户侧资源服务实现 —— 复用 admin 的 ResourceServiceImpl 过滤思路,
 * 但强制追加 status=1(只返回上架资源),返回 PortalResourceVO(精简字段)
 */
@Service
public class PortalResourceServiceImpl
        extends ServiceImpl<ResourceMapper, Resource>
        implements PortalResourceService {

    @Override
    public IPage<PortalResourceVO> pageForPortal(ResourceQueryDTO query) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        // 关键:门户侧只看上架资源,与 admin 的 page() 不同(后者可选 status)
        wrapper.eq(Resource::getStatus, 1)
                .like(StringUtils.isNotBlank(query.getResourceName()), Resource::getResourceName, query.getResourceName())
                .eq(query.getResourceType() != null, Resource::getResourceType, query.getResourceType())
                .eq(query.getCategoryId() != null, Resource::getCategoryId, query.getCategoryId())
                .eq(query.getCourseId() != null, Resource::getCourseId, query.getCourseId())
                .like(StringUtils.isNotBlank(query.getTags()), Resource::getTags, query.getTags())
                .orderByDesc(Resource::getCreateTime);

        IPage<Resource> raw = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 实体 → VO 转换
        return raw.convert(PortalResourceVO::from);
    }
}
