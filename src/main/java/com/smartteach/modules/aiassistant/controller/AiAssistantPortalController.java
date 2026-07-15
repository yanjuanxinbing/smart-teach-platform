package com.smartteach.modules.aiassistant.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.aiassistant.dto.AiSolveRequestDTO;
import com.smartteach.modules.aiassistant.service.AiAssistantService;
import com.smartteach.modules.aiassistant.vo.AiMessageVO;
import com.smartteach.modules.aiassistant.vo.AiSessionVO;
import com.smartteach.modules.aiassistant.vo.AiSolveVO;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 前台门户 - AI 解题
 * <p>
 * 接口契约：
 * <ul>
 *   <li>POST /portal/aiassistant/solve                       同步提问</li>
 *   <li>GET  /portal/aiassistant/stream?prompt=...&sessionId= 流式提问（SSE）</li>
 *   <li>GET  /portal/aiassistant/history                    我的会话列表</li>
 *   <li>GET  /portal/aiassistant/history/{sessionId}/messages 某会话的消息流水</li>
 *   <li>DELETE /portal/aiassistant/history/{sessionId}      删除会话</li>
 * </ul>
 */
@Slf4j
@Api(tags = "门户-AI 解题")
@RestController
@RequestMapping("/portal/aiassistant")
@RequiredArgsConstructor
public class AiAssistantPortalController {

    private final AiAssistantService aiService;
    private final ObjectMapper objectMapper;

    // ================================================================
    // 同步提问（一次性返回，用于流式环境异常时回退）
    // ================================================================

    @ApiOperation("AI 解题 - 同步提问（流式异常时回退使用）")
    @PostMapping("/solve")
    @PreAuthorize("isAuthenticated()")
    @OperationLog(module = "AI 解题", action = "同步提问", saveParams = false)
    public Result<AiSolveVO> solve(@Valid @RequestBody AiSolveRequestDTO dto) {
        return Result.success(aiService.solve(dto));
    }

    // ================================================================
    // 流式提问（SSE, text/event-stream）
    // ================================================================

    @ApiOperation("AI 解题 - 流式提问（SSE, 打字机效果）")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    @OperationLog(module = "AI 解题", action = "流式提问", saveParams = false)
    public SseEmitter stream(@RequestParam @NotBlank @Size(min = 2, max = 2000) String prompt,
                             @RequestParam(required = false) Long sessionId) {
        // 60s timeout；Ollama 一次生成超过 60s 即自动断流
        SseEmitter emitter = new SseEmitter(60_000L);

        // 必须在请求线程先把 userId 取出：UserContext 是 ThreadLocal，
        // CompletableFuture.runAsync 默认进 ForkJoinPool，子线程读不到原值，
        // 否则 service.requireLoginUserId() 会抛 UNAUTHORIZED → 500
        final Long userId = UserContext.getUserId();
        final String username = UserContext.getUsername();
        if (userId == null) {
            log.warn("AI 解题 /stream 未拿到登录用户，直接拒绝");
            emitter.completeWithError(new BusinessException(com.smartteach.common.result.ResultCode.UNAUTHORIZED));
            return emitter;
        }

        // 异步在 worker 线程跑整段流式回调，避免阻塞 MVC 主线程池
        CompletableFuture.runAsync(() -> {
            UserContext.setUserId(userId);
            UserContext.setUsername(username);
            try {
                aiService.streamSolve(prompt, sessionId, chunk -> {
                    // 每收到一段就推一帧 SSE 到浏览器
                    try {
                        String json = objectMapper.writeValueAsString(chunk);
                        emitter.send(SseEmitter.event()
                                .id("ai-" + System.nanoTime())
                                .name("ai-chunk")
                                .data(json));
                    } catch (JsonProcessingException e) {
                        log.warn("序列化 chunk 失败: {}", e.getMessage());
                    } catch (IOException e) {
                        // 浏览器断连/Network error — 仅警告，不阻断后续写库
                        log.info("SSE 推送失败（很可能是客户端断开）: {}", e.getMessage());
                    } catch (IllegalStateException e) {
                        // emitter 已 complete，再次 send 会抛 ISE —— 同样仅记录
                        log.info("emitter 已关闭，跳过片段: {}", e.getMessage());
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                log.error("AI 解题流式调用失败: {}", e.getMessage(), e);
                try { emitter.completeWithError(e); } catch (Exception ignored) {}
            } finally {
                UserContext.clear();
            }
        });

        return emitter;
    }

    // ================================================================
    // 历史
    // ================================================================

    @ApiOperation("我的会话列表（按 updateTime 倒序）")
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<AiSessionVO>> history(PageQuery query) {
        return Result.success(aiService.history(query));
    }

    @ApiOperation("某会话的完整消息流水（必须本人）")
    @GetMapping("/history/{sessionId}/messages")
    @PreAuthorize("isAuthenticated()")
    public Result<List<AiMessageVO>> sessionMessages(@PathVariable Long sessionId) {
        return Result.success(aiService.sessionMessages(sessionId));
    }

    @ApiOperation("删除某个会话（软删）")
    @DeleteMapping("/history/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> removeSession(@PathVariable Long sessionId) {
        aiService.removeSession(sessionId);
        return Result.success();
    }
}
