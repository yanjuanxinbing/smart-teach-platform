package com.smartteach.modules.aiassistant.config;

import com.smartteach.modules.aiassistant.properties.AiAssistantProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.util.concurrent.Semaphore;

/**
 * AI 解题 — 注册 JDK 11 HttpClient 单例 + 并发限流 Semaphore
 */
@Configuration
@RequiredArgsConstructor
public class OllamaConfig {

    private final AiAssistantProperties props;

    /** 复用一个长连接 HttpClient（HTTP/1.1 即可，Ollama 默认就支持） */
    @Bean("ollamaHttpClient")
    public HttpClient ollamaHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(props.getOllama().getConnectTimeout())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    /**
     * 流式并发限流：同一时刻最多允许多少路流式调用在本机 Ollama 上
     * （n=0 时禁用限流，传 Integer.MAX_VALUE 即可）
     */
    @Bean("aiAssistantSemaphore")
    public Semaphore aiAssistantSemaphore() {
        int permits = Math.max(props.getOllama().getStreamConcurrency(), 1);
        return new Semaphore(permits);
    }
}
