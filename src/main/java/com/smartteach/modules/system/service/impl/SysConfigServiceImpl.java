package com.smartteach.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.modules.system.dto.SysConfigSaveDTO;
import com.smartteach.modules.system.entity.SysConfig;
import com.smartteach.modules.system.mapper.SysConfigMapper;
import com.smartteach.modules.system.service.SysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public PageResult<SysConfig> page(PageQuery query) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(SysConfig::getConfigName, query.getKeyword())
                    .or().like(SysConfig::getConfigKey, query.getKeyword());
        }
        wrapper.orderByDesc(SysConfig::getCreateTime);
        IPage<SysConfig> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public String getConfigByKey(String key) {
        SysConfig config = this.lambdaQuery().eq(SysConfig::getConfigKey, key).one();
        return config == null ? null : config.getConfigValue();
    }

    @Override
    public void save(SysConfigSaveDTO dto) {
        boolean exists = this.lambdaQuery().eq(SysConfig::getConfigKey, dto.getConfigKey()).exists();
        if (exists) {
            throw new BusinessException("参数键名已存在");
        }
        SysConfig config = new SysConfig();
        BeanUtils.copyProperties(dto, config);
        if (config.getConfigType() == null) config.setConfigType(0);
        this.save(config);
    }

    @Override
    public void update(SysConfigSaveDTO dto) {
        if (dto.getConfigType() != null && dto.getConfigType() == 1) {
            throw new BusinessException("系统内置参数不可修改");
        }
        SysConfig config = new SysConfig();
        BeanUtils.copyProperties(dto, config);
        this.updateById(config);
    }

    @Override
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }
}
