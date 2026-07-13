package com.smartteach.modules.systemmonitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.profile.dto.MyLogQueryDTO;
import com.smartteach.modules.systemmonitor.entity.SysOperationLog;
import com.smartteach.modules.systemmonitor.entity.SysOperationLogQueryDTO;

import java.util.List;

public interface SysOperationLogService extends IService<SysOperationLog> {

    PageResult<SysOperationLog> page(SysOperationLogQueryDTO query);

    /**
     * 按用户筛选的"我的操作日志"分页。
     * 与 page() 同表，只额外追加 userId 限定。
     */
    PageResult<SysOperationLog> pageOfUser(Long userId, MyLogQueryDTO query);

    void clean(int beforeDays);

    void remove(List<Long> ids);
}
