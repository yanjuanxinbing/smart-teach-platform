package com.smartteach.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启用 @Async 异步支持，供操作日志切面写入日志时使用，避免阻塞业务接口
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
