package com.smartteach.modules.aiassistant.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.aiassistant.properties.AiAssistantProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 封装对本地 Ollama 的两种调用：
 *  - /api/generate：单轮（generate/stream 方法）—— 留作 fallback，文本问答场景
 *  - /api/chat：    多轮对话（chat/streamChat 方法）—— 本项目 AI 解题主路径，多轮历史
 *
 * <p>/api/chat NDJSON 响应格式示例：
 * <pre>
 *   {"model":"gemma3:1b","message":{"role":"assistant","content":"Hello"},"done":false}
 *   {"model":"gemma3:1b","message":{"role":"assistant","content":" world"},"done":false}
 *   ...
 *   {"model":"gemma3:1b","message":{"role":"assistant","content":""},"done":true,"total_duration":...,"prompt_eval_count":11,"eval_count":42}
 * </pre>
 */
@Slf4j
@Component
public class OllamaClient {

    private final AiAssistantProperties props;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OllamaClient(AiAssistantProperties props,
                        ObjectMapper objectMapper,
                        @Qualifier("ollamaHttpClient") HttpClient httpClient) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    // ================================================================
    // 同步（/solve 接口）
    // ================================================================

    /**
     * 同步调用 Ollama，返回完整文本 + 元数据
     *
     * @throws BusinessException AI_SERVICE_UNAVAILABLE / AI_RESPONSE_EMPTY
     */
    public OllamaResponse generate(String prompt) {
        HttpRequest req = buildRequest(prompt, false);
        HttpResponse<String> resp;
        try {
            resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (ConnectException e) {
            log.error("Ollama 连接失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.error("Ollama 调用异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE);
        }
        if (resp.statusCode() / 100 != 2) {
            log.error("Ollama 返回非 2xx: status={}, body={}", resp.statusCode(),
                    truncate(resp.body(), 200));
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE);
        }
        try {
            JsonNode n = objectMapper.readTree(resp.body());
            OllamaResponse out = new OllamaResponse();
            out.setModel(textOr(n, "model", props.getOllama().getModel()));
            out.setContent(textOr(n, "response", ""));
            out.setPromptEvalCount(n.path("prompt_eval_count").isNumber() ? n.path("prompt_eval_count").asLong() : null);
            out.setEvalCount(n.path("eval_count").isNumber() ? n.path("eval_count").asLong() : null);
            out.setTotalDurationNs(n.path("total_duration").isNumber() ? n.path("total_duration").asLong() : null);
            if (out.getContent() == null || out.getContent().isBlank()) {
                throw new BusinessException(ResultCode.AI_RESPONSE_EMPTY);
            }
            return out;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ollama 响应解析失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    // ================================================================
    // 流式（/stream 接口）
    // ================================================================

    /**
     * 流式调用，逐片段回调。
     *
     * @param onChunk    收到一个片段时回调（可为 null 表示丢弃）
     * @param onComplete 流完全结束时回调（拿到元数据；失败时也回调，response=null）
     * @throws BusinessException 仅在排队阶段就会立即失败；流式读到的错误通过 onComplete(response=null) 体现
     */
    public void stream(String prompt,
                       java.util.function.Consumer<String> onChunk,
                       java.util.function.Consumer<OllamaResponse> onComplete) {
        HttpRequest req = buildRequest(prompt, true);
        HttpResponse<Stream<String>> resp;
        try {
            resp = httpClient.send(req, HttpResponse.BodyHandlers.ofLines());
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.error("Ollama 流式调用发起失败: {}", e.getMessage(), e);
            onComplete.accept(null);
            return;
        }
        if (resp.statusCode() / 100 != 2) {
            log.error("Ollama 流式返回非 2xx: status={}", resp.statusCode());
            onComplete.accept(null);
            return;
        }

        StringBuilder full = new StringBuilder();
        OllamaResponse meta = new OllamaResponse();
        meta.setModel(props.getOllama().getModel());
        try {
            Iterable<String> lines = () -> resp.body().iterator();
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                try {
                    JsonNode n = objectMapper.readTree(line);
                    String chunk = n.path("response").asText("");
                    if (!chunk.isEmpty() && !"null".equals(chunk)) {
                        full.append(chunk);
                        if (onChunk != null) {
                            try { onChunk.accept(chunk); } catch (Exception ignored) { /* 前端断流，不阻断兜底 */ }
                        }
                    }
                    if (n.path("done").asBoolean(false)) {
                        meta.setPromptEvalCount(n.path("prompt_eval_count").isNumber() ? n.path("prompt_eval_count").asLong() : null);
                        meta.setEvalCount(n.path("eval_count").isNumber() ? n.path("eval_count").asLong() : null);
                        meta.setTotalDurationNs(n.path("total_duration").isNumber() ? n.path("total_duration").asLong() : null);
                    }
                } catch (Exception parseErr) {
                    log.warn("跳过一行 NDJSON 解析失败: {}", parseErr.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("读取 Ollama 流式 body 中断: {}", e.getMessage());
        } finally {
            meta.setContent(full.toString());
            if (meta.getContent().isBlank()) {
                onComplete.accept(null);   // 上层在 onComplete 见 null 即可走 AI_RESPONSE_EMPTY
            } else {
                onComplete.accept(meta);
            }
        }
    }

    // ================================================================
    // 内部
    // ================================================================

    private HttpRequest buildRequest(String prompt, boolean stream) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", props.getOllama().getModel());
            body.put("prompt", prompt);
            body.put("stream", stream);

            // 系统提示词 —— Ollama /api/generate 原生支持 system 字段（≥ 0.1.14）
            String sysPrompt = props.getOllama().getSystemPrompt();
            if (sysPrompt != null && !sysPrompt.isBlank()) {
                body.put("system", sysPrompt);
            }

            String json = objectMapper.writeValueAsString(body);

            Duration timeout = props.getOllama().getTimeout();
            return HttpRequest.newBuilder()
                    .uri(URI.create(props.getOllama().getBaseUrl() + "/api/generate"))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
        } catch (Exception e) {
            throw new BusinessException("构造 Ollama 请求失败：" + e.getMessage());
        }
    }

    // ================================================================
    // /api/chat（多轮对话推荐路径）—— 比 /api/generate 更适合带历史的会话
    // ================================================================

    /** 单条对话消息 */
    @Data
    public static class ChatMessage {
        /** system | user | assistant */
        private String role;
        private String content;

        public ChatMessage() {}
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public static ChatMessage system(String c)  { return new ChatMessage("system", c); }
        public static ChatMessage user(String c)    { return new ChatMessage("user", c); }
        public static ChatMessage assistant(String c) { return new ChatMessage("assistant", c); }
    }

    /**
     * 同步多轮对话（chat 接口，stream=false）
     *
     * @throws BusinessException AI_SERVICE_UNAVAILABLE / AI_RESPONSE_EMPTY
     */
    public OllamaResponse chat(List<ChatMessage> messages) {
        HttpRequest req = buildChatRequest(messages, false);
        HttpResponse<String> resp;
        try {
            resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (ConnectException e) {
            log.error("Ollama chat 连接失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.error("Ollama chat 调用异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE);
        }
        if (resp.statusCode() / 100 != 2) {
            log.error("Ollama chat 非 2xx: status={}, body={}", resp.statusCode(), truncate(resp.body(), 200));
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE);
        }
        try {
            JsonNode n = objectMapper.readTree(resp.body());
            OllamaResponse out = new OllamaResponse();
            out.setModel(textOr(n, "model", props.getOllama().getModel()));
            // /api/chat 非流式响应：完整内容在 message.content
            JsonNode msg = n.path("message");
            out.setContent(textOr(msg, "content", ""));
            out.setPromptEvalCount(n.path("prompt_eval_count").isNumber() ? n.path("prompt_eval_count").asLong() : null);
            out.setEvalCount(n.path("eval_count").isNumber() ? n.path("eval_count").asLong() : null);
            out.setTotalDurationNs(n.path("total_duration").isNumber() ? n.path("total_duration").asLong() : null);
            if (out.getContent() == null || out.getContent().isBlank()) {
                throw new BusinessException(ResultCode.AI_RESPONSE_EMPTY);
            }
            return out;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ollama chat 响应解析失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 流式多轮对话（chat 接口，stream=true），逐片段回调。
     *
     * @param onChunk    每收到一段 assistant 内容时回调（可为 null 表示丢弃）
     * @param onComplete 流结束时回调（成功返回元数据；失败回调 null）
     */
    public void streamChat(List<ChatMessage> messages,
                           java.util.function.Consumer<String> onChunk,
                           java.util.function.Consumer<OllamaResponse> onComplete) {
        HttpRequest req = buildChatRequest(messages, true);
        HttpResponse<Stream<String>> resp;
        try {
            resp = httpClient.send(req, HttpResponse.BodyHandlers.ofLines());
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.error("Ollama chat 流式调用发起失败: {}", e.getMessage(), e);
            onComplete.accept(null);
            return;
        }
        if (resp.statusCode() / 100 != 2) {
            log.error("Ollama chat 流式非 2xx: status={}", resp.statusCode());
            onComplete.accept(null);
            return;
        }

        StringBuilder full = new StringBuilder();
        OllamaResponse meta = new OllamaResponse();
        meta.setModel(props.getOllama().getModel());
        try {
            Iterable<String> lines = () -> resp.body().iterator();
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                try {
                    JsonNode n = objectMapper.readTree(line);
                    // /api/chat 流式：每行 {"message":{"role":"assistant","content":"delta"}, "done":false}
                    JsonNode msg = n.path("message");
                    String chunk = msg.path("content").asText("");
                    if (!chunk.isEmpty() && !"null".equals(chunk)) {
                        full.append(chunk);
                        if (onChunk != null) {
                            try { onChunk.accept(chunk); } catch (Exception ignored) { /* 前端断流，不阻断 */ }
                        }
                    }
                    if (n.path("done").asBoolean(false)) {
                        // 最后一行：{"done":true, "total_duration":..., "eval_count":..., "prompt_eval_count":...}
                        meta.setPromptEvalCount(n.path("prompt_eval_count").isNumber() ? n.path("prompt_eval_count").asLong() : null);
                        meta.setEvalCount(n.path("eval_count").isNumber() ? n.path("eval_count").asLong() : null);
                        meta.setTotalDurationNs(n.path("total_duration").isNumber() ? n.path("total_duration").asLong() : null);
                    }
                } catch (Exception parseErr) {
                    log.warn("跳过 chat NDJSON 一行: {}", parseErr.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("读取 Ollama chat 流中断: {}", e.getMessage());
        } finally {
            meta.setContent(full.toString());
            if (meta.getContent().isBlank()) {
                onComplete.accept(null);
            } else {
                onComplete.accept(meta);
            }
        }
    }

    private HttpRequest buildChatRequest(List<ChatMessage> messages, boolean stream) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", props.getOllama().getModel());
            body.put("stream", stream);
            // messages → ["role","content", "role","content", ...] 序列化为数组
            List<Map<String, String>> msgArr = new ArrayList<>(messages.size());
            for (ChatMessage m : messages) {
                Map<String, String> e = new HashMap<>(2);
                e.put("role", m.getRole());
                e.put("content", m.getContent() == null ? "" : m.getContent());
                msgArr.add(e);
            }
            body.put("messages", msgArr);

            String json = objectMapper.writeValueAsString(body);

            Duration timeout = props.getOllama().getTimeout();
            return HttpRequest.newBuilder()
                    .uri(URI.create(props.getOllama().getBaseUrl() + "/api/chat"))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
        } catch (Exception e) {
            throw new BusinessException("构造 Ollama chat 请求失败：" + e.getMessage());
        }
    }

    private static String textOr(JsonNode n, String key, String def) {
        JsonNode v = n.get(key);
        return v == null || v.isNull() ? def : v.asText(def);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    /** 调用结果/元数据 */
    @Data
    public static class OllamaResponse {
        private String model;
        private String content;
        private Long promptEvalCount;
        private Long evalCount;
        private Long totalDurationNs;
    }
}
