package com.smartteach.modules.resource.controller;

import com.smartteach.common.result.Result;
import com.smartteach.modules.resource.dto.ResourceCategorySaveDTO;
import com.smartteach.modules.resource.service.ResourceCategoryService;
import com.smartteach.modules.resource.vo.ResourceCategoryTreeVO;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 资源管理 - 资源分类
 */
@Api(tags = "资源管理-分类")
@RestController
@RequestMapping("/biz/resource/category")
@RequiredArgsConstructor
public class ResourceCategoryController {

    private final ResourceCategoryService categoryService;

    @ApiOperation("获取资源分类树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('resource:category:list')")
    public Result<List<ResourceCategoryTreeVO>> tree() {
        return Result.success(categoryService.tree());
    }

    @ApiOperation("新增分类")
    @PostMapping
    @PreAuthorize("hasAuthority('resource:category:add')")
    @OperationLog(module = "资源分类", action = "新增分类", saveParams = false)
    public Result<Void> add(@Valid @RequestBody ResourceCategorySaveDTO dto) {
        categoryService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑分类")
    @PutMapping
    @PreAuthorize("hasAuthority('resource:category:edit')")
    @OperationLog(module = "资源分类", action = "编辑分类", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody ResourceCategorySaveDTO dto) {
        categoryService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('resource:category:remove')")
    @OperationLog(module = "资源分类", action = "删除分类", saveParams = false)
    public Result<Void> remove(@PathVariable Long id) {
        categoryService.remove(id);
        return Result.success();
    }
}
