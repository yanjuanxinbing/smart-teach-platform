package com.smartteach.modules.aiassistant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.aiassistant.dto.AiSolveRequestDTO;
import com.smartteach.modules.aiassistant.entity.AiAssistantSession;
import com.smartteach.modules.aiassistant.vo.AiMessageVO;
import com.smartteach.modules.aiassistant.vo.AiSessionVO;
import com.smartteach.modules.aiassistant.vo.AiSolveVO;

import java.util.List;

/**
 * AI 解题业务接口
 */
public interface AiAssistantService extends IService<AiAssistantSession> {

    /**
     * 同步求解（一次性返回完整结果）
     */
    AiSolveVO solve(AiSolveRequestDTO dto);

    /**
     * 流式求解 —— 当前线程内同步调用 Ollama NDJSON，逐片段回调给 Controller
     *
     * @param onChunk 收到一个片段时回调（建议在 Controller 内直接 emitter.send）
     * @return 最终组装完成的会话对象（sessionId/userId/title/content/...）
     */
    StreamOutcome streamSolve(String prompt, Long sessionId,
                              java.util.function.Consumer<StreamChunk> onChunk);

    /** 当前用户会话列表（按时间倒序） */
    PageResult<AiSessionVO> history(PageQuery query);

    /** 指定会话的完整消息流水（必须本人） */
    List<AiMessageVO> sessionMessages(Long sessionId);

    /** 删除当前用户的某个会话（软删） */
    void removeSession(Long sessionId);

    /** 流式求解返回的"会话结果"，便于 Controller 写库与兜底 */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    class StreamOutcome {
        private Long sessionId;
        private String userPrompt;
        private String assistantContent;
        private Long promptEvalCount;
        private Long evalCount;
        private Long durationMs;
    }

    /** 单条片段事件（直接转 JSON `data: {delta,sessionId}` 推到前端） */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    class StreamChunk {
        private String delta;
        private Long sessionId;
    }
}
