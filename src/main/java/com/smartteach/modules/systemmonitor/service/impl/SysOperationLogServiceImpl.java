package com.smartteach.modules.systemmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.systemmonitor.entity.SysOperationLog;
import com.smartteach.modules.systemmonitor.entity.SysOperationLogQueryDTO;
import com.smartteach.modules.systemmonitor.mapper.SysOperationLogMapper;
import com.smartteach.modules.systemmonitor.service.SysOperationLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    @Override
    public PageResult<SysOperationLog> page(SysOperationLogQueryDTO query) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getModule()), SysOperationLog::getModule, query.getModule())
                .like(StringUtils.isNotBlank(query.getUsername()), SysOperationLog::getUsername, query.getUsername())
                .eq(query.getStatus() != null, SysOperationLog::getStatus, query.getStatus())
                .orderByDesc(SysOperationLog::getOperationTime);
        IPage<SysOperationLog> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public PageResult<SysOperationLog> pageOfUser(Long userId, com.smartteach.modules.profile.dto.MyLogQueryDTO query) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, SysOperationLog::getUserId, userId)
                .and(StringUtils.isNotBlank(query.getType()), w -> w.eq(SysOperationLog::getModule, query.getType())
                        .or().like(SysOperationLog::getAction, query.getType()))
                .and(StringUtils.isNotBlank(query.getKeyword()), w -> w
                        .like(SysOperationLog::getAction, query.getKeyword())
                        .or().like(SysOperationLog::getModule, query.getKeyword())
                        .or().like(SysOperationLog::getIp, query.getKeyword()))
                .orderByDesc(SysOperationLog::getOperationTime);
        IPage<SysOperationLog> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public void clean(int beforeDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(beforeDays);
        this.remove(new LambdaQueryWrapper<SysOperationLog>().lt(SysOperationLog::getOperationTime, threshold));
    }

    @Override
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }
}
