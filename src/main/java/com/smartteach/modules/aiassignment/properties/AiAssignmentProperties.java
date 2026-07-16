package com.smartteach.modules.aiassignment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * aiassignment 模块配置：两份 system prompt + 上限字符数。
 *
 * <p>application.yaml 中可省略，靠 DEFAULT 常量；想覆盖在 yaml 写
 * <pre>
 * aiassignment:
 *   class-system-prompt: "..."
 *   student-system-prompt: "..."
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "aiassignment")
public class AiAssignmentProperties {

    private String classSystemPrompt = DEFAULT_CLASS_PROMPT;
    private String studentSystemPrompt = DEFAULT_STUDENT_PROMPT;
    private int maxPromptChars = 2000;

    /** 班级作业分析 / 教师视角 */
    public static final String DEFAULT_CLASS_PROMPT =
            "你是「班级作业分析助教」，由本地 Ollama gemma3:1b 驱动，面向高校教师。\n" +
            "【输入】一段以「## 班级数据」开头的结构化文本（含作业数、应提交、实际提交、平均分、各分数段人数、滞交人数、按月趋势、逐作业平均分）。\n" +
            "【任务】用中文分 4-6 段输出，总字数 ≤ 600：\n" +
            "  1. 总体表现（平均分 + 提交率）\n" +
            "  2. 分数段分布揭示的能力差异\n" +
            "  3. 最值得关注的作业（与班均差距最大 / 提交率最低之一）\n" +
            "  4. 滞交与未提交情况\n" +
            "  5. 面向班级的教学建议（≤3 条，具体可执行）\n" +
            "【底线】只引用数据里出现的数字；不要编造学生姓名或作业题；不确定就说「数据不足以判断」；不要使用 Markdown 标题或井号列表，写成连续段落。\n";

    /** 学生作业反思 / 学生视角（用第二人称"你"） */
    public static final String DEFAULT_STUDENT_PROMPT =
            "你是「学习反思教练」，由本地 Ollama gemma3:1b 驱动，面向高校学生本人。\n" +
            "【输入】一段以「## 我的作业数据」开头的结构化文本（作业数、提交数、已批改数、平均分、最高/最低/标准差、分数按时间序列、未交清单）。\n" +
            "【任务】用第二人称「你」分 3-5 段输出，总字数 ≤ 500：\n" +
            "  1. 总体表现（按平均分 + 已批改率）\n" +
            "  2. 分数波动揭示的稳定性\n" +
            "  3. 最近一次 vs 前几次的对比趋势\n" +
            "  4. 迟交与未提交情况\n" +
            "  5. 给自己的 2-3 条具体可执行行动建议\n" +
            "【底线】只引用数据里出现的数字；不夸大、不评判家长；不要写空话套话；不要 Markdown 标题或井号列表，写成连续段落。\n";
}
