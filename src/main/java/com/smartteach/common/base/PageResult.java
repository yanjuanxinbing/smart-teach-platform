package com.smartteach.common.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * 通用分页响应结果
 */
@Data
public class PageResult<T> {

    /**
     * 透传数字序列化器 —— 压过 JacksonConfig 中全局注册的 Long→String 规则，
     * 让 total / pageNum / pageSize 仍以 JSON number 形式输出。
     *
     * <p>背景：项目用 MyBatis-Plus 雪花 ID，前端 JS Number 在 id &gt; 2^53 时丢精度，
     * 所以 JacksonConfig 把所有 Long/long 序列化为字符串。但分页元数据是 number 友好的，
     * el-pagination / el-table 都需要 number 类型，否则报 "Expected Number, got String"。
     * 字段级 @JsonSerialize 优先级高于全局 SimpleModule，因此这里能稳定压过。</p>
     */
    public static class NumberSerializer extends JsonSerializer<Number> {
        @Override
        public void serialize(Number value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            // 分页元数据都是整型,直接按 long 输出;走 Number 多态是为了让基本 long 自动装箱后命中此 serializer
            if (value instanceof Long || value instanceof Integer
                    || value instanceof Short || value instanceof Byte) {
                gen.writeNumber(value.longValue());
            } else if (value instanceof BigInteger) {
                gen.writeNumber((BigInteger) value);
            } else if (value instanceof BigDecimal) {
                gen.writeNumber((BigDecimal) value);
            } else {
                // 兜底:转字符串再 parse 回 long —— 仅用于 Double/Float 误用场景
                gen.writeNumber(new BigDecimal(value.toString()).toBigInteger());
            }
        }
    }

    @JsonSerialize(using = NumberSerializer.class)
    private long total;

    @JsonSerialize(using = NumberSerializer.class)
    private long pageNum;

    @JsonSerialize(using = NumberSerializer.class)
    private long pageSize;

    private List<T> list;

    public PageResult() {
    }

    public PageResult(IPage<T> page) {
        this.total = page.getTotal();
        this.pageNum = page.getCurrent();
        this.pageSize = page.getSize();
        this.list = page.getRecords();
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page);
    }
}
