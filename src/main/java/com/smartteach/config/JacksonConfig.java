package com.smartteach.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局序列化配置。
 *
 * <p>背景：项目使用 MyBatis-Plus 的 ASSIGN_ID（雪花算法）生成主键，雪花 ID 是 64 位 long，
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
 */
@Configuration
public class JacksonConfig {

    /**
     * 通过 Jackson2ObjectMapperBuilderCustomizer 注入自定义序列化器，
     * 避免直接替换 Spring Boot 默认的 ObjectMapper Bean，保留其自动装配的所有特性。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        SimpleModule module = new SimpleModule();
        // 对所有 Long / long 类型字段在序列化时输出为字符串
        ToStringSerializer serializer = ToStringSerializer.instance;
        module.addSerializer(Long.class, serializer);
        module.addSerializer(Long.TYPE, serializer);

        return builder -> {
            builder.modulesToInstall(module);
        };
    }
}