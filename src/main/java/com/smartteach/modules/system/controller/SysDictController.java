package com.smartteach.modules.system.controller;

import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.system.dto.SysDictDataSaveDTO;
import com.smartteach.modules.system.dto.SysDictTypeSaveDTO;
import com.smartteach.modules.system.entity.SysDictData;
import com.smartteach.modules.system.entity.SysDictType;
import com.smartteach.modules.system.service.SysDictDataService;
import com.smartteach.modules.system.service.SysDictTypeService;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 系统设置 - 数据字典
 */
@Api(tags = "系统设置-字典")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictTypeService dictTypeService;
    private final SysDictDataService dictDataService;

    // ---------- 字典类型 ----------

    @ApiOperation("分页查询字典类型")
    @GetMapping("/type/page")
    @PreAuthorize("hasAuthority('system:dict:type:list')")
    public Result<PageResult<SysDictType>> typePage(PageQuery query) {
        return Result.success(dictTypeService.page(query));
    }

    @ApiOperation("查询全部启用字典类型")
    @GetMapping("/type/list")
    public Result<List<SysDictType>> typeList() {
        return Result.success(dictTypeService.listAllEnabled());
    }

    @ApiOperation("新增字典类型")
    @PostMapping("/type")
    @PreAuthorize("hasAuthority('system:dict:type:add')")
    @OperationLog(module = "字典管理", action = "新增字典类型", saveParams = false)
    public Result<Void> addType(@Valid @RequestBody SysDictTypeSaveDTO dto) {
        dictTypeService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑字典类型")
    @PutMapping("/type")
    @PreAuthorize("hasAuthority('system:dict:type:edit')")
    @OperationLog(module = "字典管理", action = "编辑字典类型", saveParams = false)
    public Result<Void> editType(@Valid @RequestBody SysDictTypeSaveDTO dto) {
        dictTypeService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除字典类型")
    @DeleteMapping("/type")
    @PreAuthorize("hasAuthority('system:dict:type:remove')")
    @OperationLog(module = "字典管理", action = "删除字典类型", saveParams = false)
    public Result<Void> removeType(@RequestBody List<Long> ids) {
        dictTypeService.remove(ids);
        return Result.success();
    }

    // ---------- 字典数据 ----------

    @ApiOperation("分页查询字典数据")
    @GetMapping("/data/page")
    @PreAuthorize("hasAuthority('system:dict:data:list')")
    public Result<PageResult<SysDictData>> dataPage(@RequestParam(required = false) String dictType, PageQuery query) {
        return Result.success(dictDataService.page(dictType, query));
    }

    @ApiOperation("按类型查询字典数据（前端下拉使用）")
    @GetMapping("/data/list")
    public Result<List<SysDictData>> dataList(@RequestParam String dictType) {
        return Result.success(dictDataService.listByType(dictType));
    }

    @ApiOperation("新增字典数据")
    @PostMapping("/data")
    @PreAuthorize("hasAuthority('system:dict:data:add')")
    @OperationLog(module = "字典管理", action = "新增字典数据", saveParams = false)
    public Result<Void> addData(@Valid @RequestBody SysDictDataSaveDTO dto) {
        dictDataService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑字典数据")
    @PutMapping("/data")
    @PreAuthorize("hasAuthority('system:dict:data:edit')")
    @OperationLog(module = "字典管理", action = "编辑字典数据", saveParams = false)
    public Result<Void> editData(@Valid @RequestBody SysDictDataSaveDTO dto) {
        dictDataService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除字典数据")
    @DeleteMapping("/data")
    @PreAuthorize("hasAuthority('system:dict:data:remove')")
    @OperationLog(module = "字典管理", action = "删除字典数据", saveParams = false)
    public Result<Void> removeData(@RequestBody List<Long> ids) {
        dictDataService.remove(ids);
        return Result.success();
    }
}
