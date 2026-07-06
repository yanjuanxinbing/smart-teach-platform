package com.smartteach.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
        registry.addResourceHandler(accessPrefix + "**")
                .addResourceLocations("file:" + uploadPath);
    }
}
