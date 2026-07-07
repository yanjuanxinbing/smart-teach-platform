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
        // toUri() 会输出 file:///C:/xxx/ 这种标准 URI，避免 Windows 反斜杠 + 末尾斜杠拼接导致 Spring 解析失败
        Path absPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        String location = absPath.toUri().toString();
        registry.addResourceHandler(accessPrefix + "**")
                .addResourceLocations(location);
    }
}
