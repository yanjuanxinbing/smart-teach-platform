package com.smartteach.modules.aiassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.aiassistant.client.OllamaClient;
import com.smartteach.modules.aiassistant.client.OllamaClient.ChatMessage;
import com.smartteach.modules.aiassistant.dto.AiSolveRequestDTO;
import com.smartteach.modules.aiassistant.entity.AiAssistantMessage;
import com.smartteach.modules.aiassistant.entity.AiAssistantSession;
import com.smartteach.modules.aiassistant.mapper.AiAssistantSessionMapper;
import com.smartteach.modules.aiassistant.properties.AiAssistantProperties;
import com.smartteach.modules.aiassistant.service.AiAssistantService;
import com.smartteach.modules.aiassistant.vo.AiMessageVO;
import com.smartteach.modules.aiassistant.vo.AiSessionVO;
import com.smartteach.modules.aiassistant.vo.AiSolveVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AiAssistantService 实现 ——
 *  仅做：DB 读写、限流排队、调用 OllamaClient；不直接耦合 Spring MVC（SSE 由 Controller 编排）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl extends ServiceImpl<AiAssistantSessionMapper, AiAssistantSession>
        implements AiAssistantService {

    private final OllamaClient ollamaClient;
    private final AiAssistantProperties props;
    @Qualifier("aiAssistantSemaphore")
    private final Semaphore aiAssistantSemaphore;

    private final com.smartteach.modules.aiassistant.mapper.AiAssistantMessageMapper messageMapper;

    // ================================================================
    // 同步 /solve
    // ================================================================

    @Override
    public AiSolveVO solve(AiSolveRequestDTO dto) {
        Long userId = requireLoginUserId();
        String rawPrompt = dto.getPrompt();

        // 1. 会话（含新建）
        AiAssistantSession session = ensureSession(userId, dto.getSessionId(), rawPrompt);
        // 2. 拉该会话最近 N 轮历史（按 createTime asc）
        List<AiAssistantMessage> history = loadRecentHistory(session.getId());
        // 3. 拼成 /api/chat 的 messages 数组：[system, ...历史, user(当前问题)]
        List<ChatMessage> chatMessages = buildChatMessages(rawPrompt, history);
        log.info("[AI memory] /solve session={}, historyMsg={}, totalMessages={}",
                session.getId(), history.size(), chatMessages.size());
        // 4. 落库用户原问题（库里只存干净原文，不带前缀）
        saveMessage(session.getId(), userId, "user", rawPrompt, null);

        OllamaClient.OllamaResponse resp = ollamaClient.chat(chatMessages);
        saveMessage(session.getId(), userId, "assistant", resp.getContent(), resp);

        updateSessionTail(session, resp.getContent());

        AiSolveVO vo = new AiSolveVO();
        vo.setSessionId(session.getId());
        vo.setAnswer(resp.getContent());
        vo.setModel(resp.getModel());
        vo.setDurationMs(resp.getTotalDurationNs() == null ? null : resp.getTotalDurationNs() / 1_000_000L);
        vo.setPrompt(rawPrompt);
        vo.setCreatedAt(System.currentTimeMillis());
        return vo;
    }

    // ================================================================
    // 流式 /stream
    // ================================================================

    @Override
    public StreamOutcome streamSolve(String prompt, Long sessionId, java.util.function.Consumer<StreamChunk> onChunk) {
        Long userId = requireLoginUserId();
        long startNanos = System.nanoTime();

        // 限流：5 秒内抢不到许可即抛 5004
        boolean got;
        try {
            got = aiAssistantSemaphore.tryAcquire(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.AI_RATE_LIMITED);
        }
        if (!got) {
            throw new BusinessException(ResultCode.AI_RATE_LIMITED);
        }

        StreamOutcome outcome = new StreamOutcome();
        outcome.setUserPrompt(prompt);
        try {
            // 1. 准备 session（含新建）
            AiAssistantSession session = ensureSession(userId, sessionId, prompt);
            outcome.setSessionId(session.getId());
            // 2. 拉历史（按 createTime asc）
            List<AiAssistantMessage> history = loadRecentHistory(session.getId());
            // 3. 拼成 /api/chat 的 messages 数组：[system, ...历史, user(当前问题)]
            List<ChatMessage> chatMessages = buildChatMessages(prompt, history);
            log.info("[AI memory] session={}, historyMsg={}, totalMessages={}",
                    session.getId(), history.size(), chatMessages.size());
            // 4. 落库用户原问题（不存带前缀）
            saveMessage(session.getId(), userId, "user", prompt, null);

            // 5. 流式调用 Ollama /api/chat —— 把片段推给 Controller，把"完整答案"带回
            StringBuilder full = new StringBuilder();
            AtomicReference<OllamaClient.OllamaResponse> metaRef = new AtomicReference<>();

            ollamaClient.streamChat(
                    chatMessages,
                    chunk -> {
                        full.append(chunk);
                        if (onChunk != null) {
                            onChunk.accept(new StreamChunk(chunk, session.getId()));
                        }
                    },
                    finished -> {
                        // 完成（finished 可能为 null 表示流已断或 Ollama 报错）
                        metaRef.set(finished);
                    }
            );

            // 6. 写 assistant 消息 + 更新 session
            String assistant = full.toString();
            if (assistant.isBlank()) {
                throw new BusinessException(ResultCode.AI_RESPONSE_EMPTY);
            }
            OllamaClient.OllamaResponse meta = metaRef.get();
            saveMessage(session.getId(), userId, "assistant", assistant, meta);
            updateSessionTail(session, assistant);

            outcome.setAssistantContent(assistant);
            if (meta != null) {
                outcome.setPromptEvalCount(meta.getPromptEvalCount());
                outcome.setEvalCount(meta.getEvalCount());
            }
            outcome.setDurationMs((System.nanoTime() - startNanos) / 1_000_000L);
            return outcome;
        } finally {
            aiAssistantSemaphore.release();
        }
    }

    // ================================================================
    // 历史
    // ================================================================

    @Override
    public PageResult<AiSessionVO> history(PageQuery query) {
        Long userId = requireLoginUserId();
        long pageNum = query.getPageNum() <= 0 ? 1 : query.getPageNum();
        long pageSize = query.getPageSize() <= 0 ? 10 : Math.min(query.getPageSize(), 100);
        LambdaQueryWrapper<AiAssistantSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAssistantSession::getUserId, userId);
        wrapper.orderByDesc(AiAssistantSession::getUpdateTime);

        IPage<AiAssistantSession> pg = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<AiSessionVO> list = new ArrayList<>(pg.getRecords().size());
        for (AiAssistantSession s : pg.getRecords()) {
            AiSessionVO vo = new AiSessionVO();
            BeanUtils.copyProperties(s, vo);
            list.add(vo);
        }
        PageResult<AiSessionVO> result = new PageResult<>();
        result.setTotal(pg.getTotal());
        result.setPageNum(pg.getCurrent());
        result.setPageSize(pg.getSize());
        result.setList(list);
        return result;
    }

    @Override
    public List<AiMessageVO> sessionMessages(Long sessionId) {
        Long userId = requireLoginUserId();
        AiAssistantSession session = this.getOne(new LambdaQueryWrapper<AiAssistantSession>()
                .eq(AiAssistantSession::getId, sessionId)
                .eq(AiAssistantSession::getUserId, userId)
                .last("LIMIT 1"));
        if (session == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        List<AiAssistantMessage> rows = messageMapper.selectList(new LambdaQueryWrapper<AiAssistantMessage>()
                .eq(AiAssistantMessage::getSessionId, sessionId)
                .orderByAsc(AiAssistantMessage::getCreateTime));
        List<AiMessageVO> out = new ArrayList<>(rows.size());
        for (AiAssistantMessage m : rows) {
            AiMessageVO vo = new AiMessageVO();
            BeanUtils.copyProperties(m, vo);
            out.add(vo);
        }
        return out;
    }

    @Override
    public void removeSession(Long sessionId) {
        Long userId = requireLoginUserId();
        AiAssistantSession s = this.getOne(new LambdaQueryWrapper<AiAssistantSession>()
                .eq(AiAssistantSession::getId, sessionId)
                .eq(AiAssistantSession::getUserId, userId)
                .last("LIMIT 1"));
        if (s == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        this.removeById(sessionId);
    }

    // ================================================================
    // 内部工具
    // ================================================================

    private Long requireLoginUserId() {
        Long uid = UserContext.getUserId();
        if (uid == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return uid;
    }

    /**
     * 根据入参 sessionId 取已存在的 session（并校验权限），
     * 不存在 / 未传则自动创建新 session。
     */
    private AiAssistantSession ensureSession(Long userId, Long sessionId, String firstPrompt) {
        if (sessionId != null) {
            AiAssistantSession existed = this.getOne(new LambdaQueryWrapper<AiAssistantSession>()
                    .eq(AiAssistantSession::getId, sessionId)
                    .last("LIMIT 1"));
            if (existed == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST);
            }
            if (!userId.equals(existed.getUserId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            return existed;
        }
        AiAssistantSession s = new AiAssistantSession();
        s.setUserId(userId);
        s.setTitle(buildTitle(firstPrompt));
        s.setStatus(1);
        this.save(s);
        return s;
    }

    private String buildTitle(String prompt) {
        if (prompt == null) return "新会话";
        String t = prompt.trim().replaceAll("\\s+", " ");
        return t.length() > 30 ? t.substring(0, 30) + "…" : t;
    }

    private void saveMessage(Long sessionId, Long userId, String role, String content, OllamaClient.OllamaResponse meta) {
        AiAssistantMessage m = new AiAssistantMessage();
        m.setSessionId(sessionId);
        m.setUserId(userId);
        m.setRole(role);
        m.setContent(content);
        if (meta != null) {
            m.setPromptEvalCount(meta.getPromptEvalCount() == null ? null : meta.getPromptEvalCount().intValue());
            m.setEvalCount(meta.getEvalCount() == null ? null : meta.getEvalCount().intValue());
        }
        messageMapper.insert(m);
    }

    private void updateSessionTail(AiAssistantSession session, String lastAssistant) {
        // 触发 BaseEntity 的 updateTime 与 update_by 自动填充
        session.setTitle(buildTitle(lastAssistant == null ? null : lastAssistant));
        this.updateById(session);
    }

    /**
     * 拉取该会话最近 N 轮问答消息（按 createTime asc 返回，Ollama 看顺序），
     * 在保存"本次用户问题"之前调用 —— 故历史里不含本次新问。
     */
    private List<AiAssistantMessage> loadRecentHistory(Long sessionId) {
        int maxTurns = Math.max(props.getOllama().getHistoryMaxTurns(), 0);
        if (maxTurns == 0 || sessionId == null) {
            return Collections.emptyList();
        }
        // 取 desc 限行，再在内存中反转回 asc —— MyBatis-Plus 3.5 没有 lastReverse 之类的便捷 API
        List<AiAssistantMessage> desc = messageMapper.selectList(new LambdaQueryWrapper<AiAssistantMessage>()
                .eq(AiAssistantMessage::getSessionId, sessionId)
                .orderByDesc(AiAssistantMessage::getCreateTime)
                .last("LIMIT " + (maxTurns * 2)));
        if (desc == null || desc.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.reverse(desc);
        return desc;
    }

    /**
     * 把"系统提示词 + 历史问答 + 当前问题"拼成 /api/chat 用的 messages 数组。
     *  - system 仅在数组为空时也会下发（Ollama 必填 role=system），便于未配置时也能正常对话
     *  - 历史按 user/assistant 角色原样转发
     *  - 历史里单条 content 太长会按 per-message-chars 截断（避免 prompt 爆炸）
     */
    private List<ChatMessage> buildChatMessages(String currentPrompt, List<AiAssistantMessage> history) {
        List<ChatMessage> msgs = new ArrayList<>();
        String sys = props.getOllama().getSystemPrompt();
        if (sys != null && !sys.isBlank()) {
            msgs.add(ChatMessage.system(sys));
        }

        int perMsg = Math.max(props.getOllama().getHistoryPerMessageChars(), 50);
        for (AiAssistantMessage m : history) {
            String role = m.getRole();
            if (!"user".equals(role) && !"assistant".equals(role)) continue;
            String content = m.getContent();
            if (content == null) continue;
            if (content.length() > perMsg) {
                content = content.substring(0, perMsg) + "…";
            }
            msgs.add(new ChatMessage(role, content));
        }

        msgs.add(ChatMessage.user(currentPrompt));
        return msgs;
    }
}
