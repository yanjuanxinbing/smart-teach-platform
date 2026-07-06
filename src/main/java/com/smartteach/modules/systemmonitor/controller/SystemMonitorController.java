package com.smartteach.modules.systemmonitor.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.systemmonitor.entity.SysLoginLog;
import com.smartteach.modules.systemmonitor.entity.SysOperationLog;
import com.smartteach.modules.systemmonitor.entity.SysOperationLogQueryDTO;
import com.smartteach.modules.systemmonitor.service.ServerMonitorService;
import com.smartteach.modules.systemmonitor.service.SysLoginLogService;
import com.smartteach.modules.systemmonitor.service.SysOperationLogService;
import com.smartteach.modules.systemmonitor.vo.ServerInfoVO;
import com.smartteach.common.base.PageQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统监控模块：服务器实时状态 + 登录日志 + 操作日志
 */
@Api(tags = "系统监控")
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class SystemMonitorController {

    private final ServerMonitorService serverMonitorService;
    private final SysLoginLogService loginLogService;
    private final SysOperationLogService operationLogService;

    @ApiOperation("获取服务器CPU/内存/磁盘/JVM实时状态")
    @GetMapping("/server")
    @PreAuthorize("hasAuthority('monitor:server:list')")
    public Result<ServerInfoVO> server() {
        return Result.success(serverMonitorService.getServerInfo());
    }

    @ApiOperation("分页查询登录日志")
    @GetMapping("/login-log/page")
    @PreAuthorize("hasAuthority('monitor:loginLog:list')")
    public Result<PageResult<SysLoginLog>> loginLogPage(PageQuery query) {
        return Result.success(loginLogService.page(query));
    }

    @ApiOperation("分页查询操作日志")
    @GetMapping("/operation-log/page")
    @PreAuthorize("hasAuthority('monitor:operationLog:list')")
    public Result<PageResult<SysOperationLog>> operationLogPage(SysOperationLogQueryDTO query) {
        return Result.success(operationLogService.page(query));
    }

    @ApiOperation("清理N天前的操作日志")
    @DeleteMapping("/operation-log/clean")
    @PreAuthorize("hasAuthority('monitor:operationLog:remove')")
    public Result<Void> clean(@RequestParam(defaultValue = "30") Integer beforeDays) {
        operationLogService.clean(beforeDays);
        return Result.success();
    }

    @ApiOperation("批量删除操作日志")
    @DeleteMapping("/operation-log")
    @PreAuthorize("hasAuthority('monitor:operationLog:remove')")
    public Result<Void> removeOperationLog(@RequestBody List<Long> ids) {
        operationLogService.remove(ids);
        return Result.success();
    }
}
