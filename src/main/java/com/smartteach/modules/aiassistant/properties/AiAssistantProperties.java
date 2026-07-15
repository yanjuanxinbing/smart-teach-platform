package com.smartteach.modules.aiassistant.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 读取 application.yaml 中 aiassistant.* 配置
 * @see <pre>aiassistant.ollama.base-url / model / timeout / connect-timeout / stream-concurrency</pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "aiassistant")
public class AiAssistantProperties {

    private Ollama ollama = new Ollama();
    private History history = new History();

    @Data
    public static class Ollama {
        private String baseUrl = "http://localhost:11434";
        private String model = "gemma3:1b";
        private Duration timeout = Duration.ofSeconds(60);
        private Duration connectTimeout = Duration.ofSeconds(5);
        /** 同时在飞的最大请求数（>0 时启用 Semaphore 限流） */
        private int streamConcurrency = 2;

        /**
         * 系统提示词（system role），gemma3:1b 通过 Ollama /api/generate 的 system 字段下发。
         * 默认设为「解题助教」角色：分步骤、重推导、注语言跟随、不确定时明说、不写最终大段结论。
         */
        private String systemPrompt = DEFAULT_SYSTEM_PROMPT;

        /**
         * 上下文记忆 — 同一会话内最多保留最近多少轮问答（即多少对 user/assistant），
         * 超出部分按 createTime 截断。每条消息再按 per-message-chars 截断避免 prompt 爆炸。
         * 不区分流式/非流式；为 0 表示完全不用历史。
         */
        private int historyMaxTurns = 8;

        /** 单条历史消息的最大字符数；超出截断（500 ≈ 中文 200~300 字） */
        private int historyPerMessageChars = 800;
    }

    /**
     * 默认教学助教 system prompt —— 与 properties 中默认值保持一致，方便 IDE 跳转阅读。
     */
    public static final String DEFAULT_SYSTEM_PROMPT =
            "你是「AI 解题助手」，面向高校本科生的学习辅助角色，由本地 Ollama 驱动。\n" +
            "【任务】帮助学生理解问题、引导思路，而不是直接给出最终答案。\n" +
            "【风格】\n" +
            "1. 先用 1-2 句话概述思路，再按编号步骤展开；\n" +
            "2. 涉及代码时，给出最小可运行片段（带必要注释），并配 1-2 行关键解释；\n" +
            "3. 涉及数学/证明时，明确每一步依据（公式 / 定理 / 定义）；\n" +
            "4. 输出语言跟随提问语言（中文提问用中文回答，英文提问用英文回答）；\n" +
            "5. 内容尽量准确；不确定时直接说明「我不确定」，绝不编造；\n" +
            "6. 末尾用 1-3 条「关键点」总结要点，不要写大段结语；\n" +
            "7. 单次回答不超过 600 字，除非用户明确要求更详细。\n" +
            "【底线】你只是辅助，学生必须独立完成作业与考试。";


    @Data
    public static class History {
        /** 每用户保留的最大会话数；超出按 update_time 删旧 */
        private int keepPerUser = 200;
    }
}
