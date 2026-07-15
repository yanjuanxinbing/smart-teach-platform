package com.smartteach.modules.resource.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.resource.dto.ResourceQueryDTO;
import com.smartteach.modules.resource.entity.Resource;
import com.smartteach.modules.resource.service.PortalResourceService;
import com.smartteach.modules.resource.service.ResourceService;
import com.smartteach.modules.resource.vo.PortalResourceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 门户侧资源接口 —— 学生查看 / 下载管理员上传的教学资源
 *
 * <p>复用 admin 的 biz_resource 表(全局资源),但只暴露上架(status=1)数据</p>
 * <p>对接前端契约见 web-portal/src/api/resource.js</p>
 *
 * <p>为什么不用 admin 的 /biz/resource 系列接口?</p>
 * <ul>
 *   <li>admin 接口要求 @PreAuthorize('resource:list') —— 学生角色无此权限串(见 init.sql)</li>
 *   <li>admin 接口 status 过滤是可选的,门户需要强制只看上架资源</li>
 *   <li>admin 接口返回完整 Resource entity,泄露 file_path / create_by / update_by 等内部字段</li>
 * </ul>
 */
@Api(tags = "门户-资源")
@RestController
@RequestMapping("/portal/resource")
@RequiredArgsConstructor
public class PortalResourceController {

    private final PortalResourceService portalResourceService;
    private final ResourceService resourceService;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-prefix}")
    private String accessPrefix;

    @ApiOperation("资源列表(仅上架)")
    @GetMapping("/page")
    public Result<PageResult<PortalResourceVO>> page(ResourceQueryDTO query) {
        IPage<PortalResourceVO> p = portalResourceService.pageForPortal(query);
        return Result.success(PageResult.of(p));
    }

    @ApiOperation("资源详情(仅上架,并自增浏览数)")
    @GetMapping("/{id}")
    public Result<PortalResourceVO> detail(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        resourceService.incrementViewCount(id);
        return Result.success(PortalResourceVO.from(resource));
    }

    @ApiOperation("下载资源文件(仅上架,强制浏览器下载)")
    @GetMapping("/{id}/file")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // fileUrl 形如 "/api/files/2025/01/uuid.pptx",去掉 accessPrefix 得到相对磁盘路径
        String relativePath = resource.getFileUrl();
        if (relativePath != null && accessPrefix != null && relativePath.startsWith(accessPrefix)) {
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

        // 自增下载次数
        resourceService.incrementDownloadCount(id);

        // 用原始文件名,处理中文/空格/百分号
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
