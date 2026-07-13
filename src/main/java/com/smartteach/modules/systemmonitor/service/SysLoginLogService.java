package com.smartteach.modules.systemmonitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.systemmonitor.entity.SysLoginLog;

import java.util.List;

public interface SysLoginLogService extends IService<SysLoginLog> {

    void record(String username, String ip, String userAgent, int status, String message);

    PageResult<SysLoginLog> page(PageQuery query);

    void remove(List<Long> ids);

    /** 统计今日成功登录次数（按 loginTime >= 今日零点） */
    long countTodaySuccess();
}
