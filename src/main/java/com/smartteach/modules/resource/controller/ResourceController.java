package com.smartteach.modules.resource.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.resource.dto.ResourceQueryDTO;
import com.smartteach.modules.resource.dto.ResourceSaveDTO;
import com.smartteach.modules.resource.entity.Resource;
import com.smartteach.modules.resource.service.ResourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 资源管理 - 教学资源维护
 */
@Api(tags = "资源管理-资源")
@RestController
@RequestMapping("/biz/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @ApiOperation("分页查询资源")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('resource:list')")
    public Result<PageResult<Resource>> page(ResourceQueryDTO query) {
        return Result.success(resourceService.page(query));
    }

    @ApiOperation("获取资源详情（并自增浏览数）")
    @GetMapping("/{id}")
    public Result<Resource> detail(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource != null && resource.getStatus() != null && resource.getStatus() == 1) {
            resourceService.incrementViewCount(id);
        }
        return Result.success(resource);
    }

    @ApiOperation("新增资源")
    @PostMapping
    @PreAuthorize("hasAuthority('resource:add')")
    public Result<Void> add(@Valid @RequestBody ResourceSaveDTO dto) {
        resourceService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑资源")
    @PutMapping
    @PreAuthorize("hasAuthority('resource:edit')")
    public Result<Void> edit(@Valid @RequestBody ResourceSaveDTO dto) {
        resourceService.update(dto);
        return Result.success();
    }

    @ApiOperation("批量删除资源")
    @DeleteMapping
    @PreAuthorize("hasAuthority('resource:remove')")
    public Result<Void> remove(@RequestBody List<Long> ids) {
        resourceService.remove(ids);
        return Result.success();
    }

    @ApiOperation("上架/下架资源")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('resource:edit')")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        resourceService.changeStatus(id, status);
        return Result.success();
    }

    @ApiOperation("下载资源（自增下载次数）")
    @PostMapping("/{id}/download")
    public Result<Void> download(@PathVariable Long id) {
        resourceService.incrementDownloadCount(id);
        return Result.success();
    }
}
