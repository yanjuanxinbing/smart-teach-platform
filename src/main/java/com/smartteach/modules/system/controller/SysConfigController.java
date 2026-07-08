package com.smartteach.modules.system.controller;

import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.system.dto.SysConfigSaveDTO;
import com.smartteach.modules.system.entity.SysConfig;
import com.smartteach.modules.system.service.SysConfigService;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 系统设置 - 参数配置
 */
@Api(tags = "系统设置-参数配置")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    @ApiOperation("分页查询参数")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<PageResult<SysConfig>> page(PageQuery query) {
        return Result.success(configService.page(query));
    }

    @ApiOperation("根据键名获取参数值（公开）")
    @GetMapping("/value")
    public Result<String> getValue(@RequestParam String key) {
        return Result.success(configService.getConfigByKey(key));
    }

    @ApiOperation("新增参数")
    @PostMapping
    @PreAuthorize("hasAuthority('system:config:add')")
    @OperationLog(module = "参数配置", action = "新增参数", saveParams = false)
    public Result<Void> add(@Valid @RequestBody SysConfigSaveDTO dto) {
        configService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑参数")
    @PutMapping
    @PreAuthorize("hasAuthority('system:config:edit')")
    @OperationLog(module = "参数配置", action = "编辑参数", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody SysConfigSaveDTO dto) {
        configService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除参数")
    @DeleteMapping
    @PreAuthorize("hasAuthority('system:config:remove')")
    @OperationLog(module = "参数配置", action = "删除参数", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        configService.remove(ids);
        return Result.success();
    }
}
