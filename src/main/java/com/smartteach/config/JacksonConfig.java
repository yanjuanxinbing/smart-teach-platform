package com.smartteach.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Jackson 全局序列化配置。
 *
 * <p>背景 1：项目使用 MyBatis-Plus 的 ASSIGN_ID（雪花算法）生成主键，雪花 ID 是 64 位 long，
 * 序列化到 JSON 时如果直接输出为数字，前端 JavaScript 会按 Number 类型解析，
 * 而 JS 的 Number 最大安全整数是 2^53 - 1（即 9007199254740991），
 * 超过这个范围会丢失精度，导致回传给后端的 ID 与数据库实际 ID 不一致，
 * 进而出现"按钮点了没反应"的现象（后端按错误 ID 查不到记录）。
 *
 * <p>解决：将所有 {@code Long} / {@code long} 类型字段在序列化时统一转为字符串返回，
 * 前端按字符串透传即可，Spring/Jackson 反序列化时会把字符串自动转回 Long，不影响业务。
 *
 * <p>注意：{@code PageResult} 里的 total/pageNum/pageSize 是 long 基本类型，
 * 仍然会以数字形式返回，不会被本规则影响。
 *
 * <p>背景 2：前端 Element Plus 的 {@code el-date-picker} 习惯用
 * {@code value-format="YYYY-MM-DD HH:mm:ss"} 把 datetime 输出为带空格的字符串，
 * Jackson 自带的 {@code LocalDateTimeDeserializer} 只认 ISO-8601（带 {@code T}），
 * 会抛 "Text '...' could not be parsed at index 10"。这里把时间类型统一改成
 * 同时认 ISO 与 {@code yyyy-MM-dd HH:mm:ss}，反序列化时两个格式都接受。
 */
@Configuration
public class JacksonConfig {

    private static final String ISO_DATETIME = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String SPACE_DATETIME = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE = "yyyy-MM-dd";
    private static final String TIME = "HH:mm:ss";

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern(ISO_DATETIME),
            DateTimeFormatter.ofPattern(SPACE_DATETIME)
    );

    /**
     * 通过 Jackson2ObjectMapperBuilderCustomizer 注入自定义序列化器，
     * 避免直接替换 Spring Boot 默认的 ObjectMapper Bean，保留其自动装配的所有特性。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        SimpleModule module = new SimpleModule();

        // 1) Long / long 序列化为字符串，避免前端 JS 精度丢失
        ToStringSerializer serializer = ToStringSerializer.instance;
        module.addSerializer(Long.class, serializer);
        module.addSerializer(Long.TYPE, serializer);

        // 2) LocalDateTime：序列化 ISO；反序列化同时认 ISO 与 "yyyy-MM-dd HH:mm:ss"
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(ISO_DATETIME)));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getValueAsString();
                if (text == null || text.isEmpty()) return null;
                for (DateTimeFormatter f : DATE_TIME_FORMATTERS) {
                    try { return LocalDateTime.parse(text, f); } catch (Exception ignore) { }
                }
                return (LocalDateTime) ctxt.handleWeirdStringValue(LocalDateTime.class, text,
                        "无法解析日期时间，期望格式 yyyy-MM-dd'T'HH:mm:ss 或 yyyy-MM-dd HH:mm:ss");
            }
        });

        // 3) LocalDate / LocalTime 顺手统一
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE)));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE)));
        module.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME)));
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME)));

        return builder -> {
            builder.modulesToInstall(module);
        };
    }
}
