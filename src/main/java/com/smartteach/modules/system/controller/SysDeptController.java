package com.smartteach.modules.system.controller;

import com.smartteach.common.result.Result;
import com.smartteach.modules.system.dto.SysDeptSaveDTO;
import com.smartteach.modules.system.service.SysDeptService;
import com.smartteach.modules.system.vo.SysDeptTreeVO;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 系统设置 - 部门管理
 */
@Api(tags = "系统设置-部门")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @ApiOperation("获取部门树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:dept:list')")
    public Result<List<SysDeptTreeVO>> tree() {
        return Result.success(deptService.tree());
    }

    @ApiOperation("新增部门")
    @PostMapping
    @PreAuthorize("hasAuthority('system:dept:add')")
    @OperationLog(module = "部门管理", action = "新增部门", saveParams = false)
    public Result<Void> add(@Valid @RequestBody SysDeptSaveDTO dto) {
        deptService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑部门")
    @PutMapping
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @OperationLog(module = "部门管理", action = "编辑部门", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody SysDeptSaveDTO dto) {
        deptService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:remove')")
    @OperationLog(module = "部门管理", action = "删除部门", saveParams = false)
    public Result<Void> remove(@PathVariable Long id) {
        deptService.remove(id);
        return Result.success();
    }
}
