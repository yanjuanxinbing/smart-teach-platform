package com.smartteach.modules.resource.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.resource.dto.ResourceQueryDTO;
import com.smartteach.modules.resource.dto.ResourceSaveDTO;
import com.smartteach.modules.resource.entity.Resource;
import com.smartteach.modules.resource.service.ResourceService;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-prefix}")
    private String accessPrefix;

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
    @OperationLog(module = "资源管理", action = "新增资源", saveParams = false)
    public Result<Void> add(@Valid @RequestBody ResourceSaveDTO dto) {
        resourceService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑资源")
    @PutMapping
    @PreAuthorize("hasAuthority('resource:edit')")
    @OperationLog(module = "资源管理", action = "编辑资源", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody ResourceSaveDTO dto) {
        resourceService.update(dto);
        return Result.success();
    }

    @ApiOperation("批量删除资源")
    @DeleteMapping
    @PreAuthorize("hasAuthority('resource:remove')")
    @OperationLog(module = "资源管理", action = "批量删除资源", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        resourceService.remove(ids);
        return Result.success();
    }

    @ApiOperation("上架/下架资源")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('resource:edit')")
    @OperationLog(module = "资源管理", action = "修改资源上下架", saveParams = false)
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

    @ApiOperation("下载资源文件（强制浏览器下载，含 Content-Disposition: attachment）")
    @GetMapping("/{id}/file")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // fileUrl 形如 "/api/files/2025/01/uuid.pptx"，去掉 accessPrefix 得到相对磁盘路径
        String relativePath = resource.getFileUrl();
        if (relativePath != null && relativePath.startsWith(accessPrefix)) {
            relativePath = relativePath.substring(accessPrefix.length());
        }
        if (relativePath == null || relativePath.isEmpty()) {
            throw new BusinessException("文件路径无效");
        }

        Path basePath = Paths.get(uploadPath).toAbsolutePath().normalize();
        File file = basePath.resolve(relativePath).toFile();
        if (!file.exists()) {
            throw new BusinessException(ResultCode.FILE_NOT_EXIST);
        }

        // 同步自增下载次数
        resourceService.incrementDownloadCount(id);

        // 用原始文件名（处理中文/空格/百分号），双形式兼顾老旧浏览器
        String filename = resource.getOriginalName() != null ? resource.getOriginalName() : resource.getResourceName();
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded)
                .contentLength(file.length())
                .body(new FileSystemResource(file));
    }
}
