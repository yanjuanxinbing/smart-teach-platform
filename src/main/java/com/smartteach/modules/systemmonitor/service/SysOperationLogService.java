package com.smartteach.modules.systemmonitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.systemmonitor.entity.SysOperationLog;
import com.smartteach.modules.systemmonitor.entity.SysOperationLogQueryDTO;

import java.util.List;

public interface SysOperationLogService extends IService<SysOperationLog> {

    PageResult<SysOperationLog> page(SysOperationLogQueryDTO query);

    void clean(int beforeDays);

    void remove(List<Long> ids);
}
