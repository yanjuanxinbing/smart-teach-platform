package com.smartteach.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.modules.system.dto.SysDictTypeSaveDTO;
import com.smartteach.modules.system.entity.SysDictType;
import com.smartteach.modules.system.mapper.SysDictTypeMapper;
import com.smartteach.modules.system.service.SysDictTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public PageResult<SysDictType> page(PageQuery query) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(SysDictType::getDictName, query.getKeyword())
                    .or().like(SysDictType::getDictType, query.getKeyword());
        }
        wrapper.orderByDesc(SysDictType::getCreateTime);
        IPage<SysDictType> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public void save(SysDictTypeSaveDTO dto) {
        boolean exists = this.lambdaQuery().eq(SysDictType::getDictType, dto.getDictType()).exists();
        if (exists) {
            throw new BusinessException("字典类型已存在");
        }
        SysDictType type = new SysDictType();
        BeanUtils.copyProperties(dto, type);
        if (type.getStatus() == null) type.setStatus(1);
        this.save(type);
    }

    @Override
    public void update(SysDictTypeSaveDTO dto) {
        SysDictType type = new SysDictType();
        BeanUtils.copyProperties(dto, type);
        this.updateById(type);
    }

    @Override
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<SysDictType> listAllEnabled() {
        return this.lambdaQuery().eq(SysDictType::getStatus, 1)
                .orderByAsc(SysDictType::getDictType)
                .list();
    }
}
