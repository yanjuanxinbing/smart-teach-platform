package com.smartteach.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson tweak: serialize every Long/long field as a quoted string in JSON responses.
 *
 * Why: this project uses MyBatis-Plus IdType.ASSIGN_ID (Snowflake), which produces
 * 19-digit primary keys. Those exceed JavaScript's Number.MAX_SAFE_INTEGER (2^53-1).
 * If we let a Long serialize as a bare number, the browser truncates the low-order
 * bits silently, and any subsequent echo of the id (DELETE /xxx/{id}, PUT body, ...)
 * either sends a wrong integer or a scientific-notation string that the server
 * cannot parse (Long.parseLong("2.07E+18") -> NumberFormatException -> HTTP 500).
 *
 * By emitting every id as a quoted string we keep the round-trip exact. Frontends
 * can pass the string back unchanged; Jackson deserializes "2074368061289897985"
 * into Long just fine.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longAsStringCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}