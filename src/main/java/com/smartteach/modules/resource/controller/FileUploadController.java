package com.smartteach.modules.resource.controller;

import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import com.smartteach.modules.resource.storage.FileStorage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Resource module file upload. The actual storage backend is selected by
 * file.storage-type in application.yaml (local / oss / minio). See FileStorageAutoConfig.
 *
 * Swagger annotations were intentionally omitted to avoid pulling in a swagger
 * dependency on the request-side path; knife4j is still used for listing this
 * endpoint in the global API doc, just without per-method decoration here.
 */
@RestController
@RequestMapping("/biz/resource/upload")
public class FileUploadController {

    private final FileStorage fileStorage;

    public FileUploadController(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @PostMapping
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        try {
            String datePath = LocalDate.now().toString().replace("-", "/");
            String key = fileStorage.store(datePath, file);
            String accessUrl = fileStorage.getAccessUrl(key);

            Map<String, Object> result = new HashMap<>();
            result.put("originalName", file.getOriginalFilename());
            result.put("fileUrl", accessUrl);
            result.put("fileSize", file.getSize());
            result.put("fileSuffix", suffixOf(file.getOriginalFilename()));
            result.put("storageKey", key);
            return Result.success(result);
        } catch (Exception e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    private static String suffixOf(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1);
    }
}