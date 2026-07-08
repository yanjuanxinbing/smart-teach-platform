package com.smartteach.modules.resource.controller;

import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 资源管理模块 - 文件上传接口。
 * 生产环境建议替换为对象存储（阿里云OSS/MinIO等），当前实现为本地磁盘存储示例。
 */
@Api(tags = "资源管理-文件上传")
@RestController
@RequestMapping("/biz/resource/upload")
public class FileUploadController {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-prefix}")
    private String accessPrefix;

    @ApiOperation("上传文件")
    @PostMapping
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        try {
            String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
            String datePath = LocalDate.now().toString().replace("-", "/");
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;

            // toAbsolutePath() 兜底：避免在 Windows / Tomcat 嵌入模式下，相对路径被解析到临时目录
            Path basePath = Paths.get(uploadPath).toAbsolutePath().normalize();
            File dir = basePath.resolve(datePath).toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            // 同步回写实际使用的访问前缀，方便后续改用对象存储等只换 URL 规则的情况
            String accessUrl = accessPrefix + datePath + "/" + fileName;

            Map<String, Object> result = new HashMap<>();
            result.put("originalName", file.getOriginalFilename());
            result.put("fileUrl", accessUrl);
            result.put("fileSize", file.getSize());
            result.put("fileSuffix", suffix);
            return Result.success(result);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }
}
