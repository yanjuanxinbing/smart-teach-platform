package com.smartteach.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 将本地磁盘的上传目录映射为可通过 URL 直接访问的静态资源路径，
 * 供"资源管理"模块的课件/视频/图片等文件预览下载使用。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-prefix}")
    private String accessPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve the configured upload-path to an absolute, normalized path.
        Path absPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        // Build a resource location Spring can open. On Windows, absPath looks
        // like D:\data\...  We need 'file:///' (three slashes) so Spring treats
        // it as an absolute file URL. 'file://D:/...' (two slashes) is broken on
        // Windows because the leading '/' before the drive letter is invalid.
        String abs = absPath.toString().replace('\\', '/');
        // On Windows, file://D:/... has URL.path = /D:/... which File resolves as D:\\D:\\... (bad).
        // Use file:D:/... (no leading slash) so URL.path = D:/...  File(D:/...) is correct.
        String location = abs.matches("^[A-Za-z]:.*") ? ("file:" + abs) : ("file://" + abs);
        registry.addResourceHandler(accessPrefix + "**")
                .addResourceLocations(location);
    }
}
