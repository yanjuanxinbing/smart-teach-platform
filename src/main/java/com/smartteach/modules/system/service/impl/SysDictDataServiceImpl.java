package com.smartteach.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.system.dto.SysDictDataSaveDTO;
import com.smartteach.modules.system.entity.SysDictData;
import com.smartteach.modules.system.mapper.SysDictDataMapper;
import com.smartteach.modules.system.service.SysDictDataService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Override
    public PageResult<SysDictData> page(String dictType, PageQuery query) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(dictType), SysDictData::getDictType, dictType)
                .like(StringUtils.isNotBlank(query.getKeyword()), SysDictData::getDictLabel, query.getKeyword())
                .orderByAsc(SysDictData::getSort);
        IPage<SysDictData> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<SysDictData> listByType(String dictType) {
        return this.lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSort)
                .list();
    }

    @Override
    public void save(SysDictDataSaveDTO dto) {
        SysDictData data = new SysDictData();
        BeanUtils.copyProperties(dto, data);
        if (data.getStatus() == null) data.setStatus(1);
        if (data.getIsDefault() == null) data.setIsDefault(0);
        this.save(data);
    }

    @Override
    public void update(SysDictDataSaveDTO dto) {
        SysDictData data = new SysDictData();
        BeanUtils.copyProperties(dto, data);
        this.updateById(data);
    }

    @Override
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }
}
