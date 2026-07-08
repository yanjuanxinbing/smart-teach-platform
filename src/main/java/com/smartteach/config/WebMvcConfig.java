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
 *
 * 关于文本文件编码：ResourceHandlerRegistration.setMediaTypes() 是 Spring 6 才加入的 API，
 * 当前项目是 Spring Boot 2.7.x 没有该方法。文本乱码问题改在文本预览路径上用
 * 前端 fetch().text()（浏览器默认 UTF-8 解码）规避，因此服务端不再做额外处理。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-prefix}")
    private String accessPrefix;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // toUri() 会输出 file:///C:/xxx/ 这种标准 URI，避免 Windows 反斜杠 + 末尾斜杠拼接导致 Spring 解析失败
        Path absPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        String location = absPath.toUri().toString();

        // addResourceHandler 匹配的是 servlet 内部路径（context-path 已被剥离），
        // 而 accessPrefix 是前端可见的完整 URL（含 context-path），需要去掉 context-path 前缀，
        // 否则永远匹配不到资源 → 预览/下载返回 404
        String handlerPrefix = accessPrefix;
        if (contextPath != null && !contextPath.isEmpty() && handlerPrefix.startsWith(contextPath + "/")) {
            handlerPrefix = handlerPrefix.substring(contextPath.length());
        }

        registry.addResourceHandler(handlerPrefix + "**")
                .addResourceLocations(location);
    }
}
