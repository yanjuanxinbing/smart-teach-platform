package com.smartteach.modules.systemmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.systemmonitor.entity.SysLoginLog;
import com.smartteach.modules.systemmonitor.mapper.SysLoginLogMapper;
import com.smartteach.modules.systemmonitor.service.SysLoginLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public void record(String username, String ip, String userAgent, int status, String message) {
        SysLoginLog log = new SysLoginLog();
        log.setUsername(username);
        log.setIp(ip);
        log.setBrowser(parseBrowser(userAgent));
        log.setOs(parseOs(userAgent));
        log.setStatus(status);
        log.setMessage(message);
        log.setLoginTime(LocalDateTime.now());
        this.save(log);
    }

    @Override
    public PageResult<SysLoginLog> page(PageQuery query) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getKeyword()), SysLoginLog::getUsername, query.getKeyword())
                .orderByDesc(SysLoginLog::getLoginTime);
        IPage<SysLoginLog> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    /** 简单UA解析，生产环境建议使用 UserAgentUtils 等专业库 */
    private String parseBrowser(String ua) {
        if (StringUtils.isBlank(ua)) return "未知";
        if (ua.contains("Edg/")) return "Edge";
        if (ua.contains("Chrome/")) return "Chrome";
        if (ua.contains("Firefox/")) return "Firefox";
        if (ua.contains("Safari/") && !ua.contains("Chrome")) return "Safari";
        return "其他";
    }

    private String parseOs(String ua) {
        if (StringUtils.isBlank(ua)) return "未知";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS")) return "macOS";
        if (ua.contains("Linux")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        return "其他";
    }
}
