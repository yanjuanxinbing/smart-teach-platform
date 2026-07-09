package com.smartteach.modules.systemmonitor.service;

import com.smartteach.modules.systemmonitor.entity.SysOperationLog;
import com.smartteach.modules.systemmonitor.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 单独抽出 @Async 写入操作日志。
 *
 * <p>原因：Spring 的 @Async 必须经过代理调用才会真正异步执行，
 * 切面类内部 {@code this.saveLog(...)} 会绕过代理，注解完全失效。
 * 将写入动作抽到独立 Bean 中，由切面注入并调用，@Async 才能生效。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogWriter {

    private final SysOperationLogMapper operationLogMapper;

    @Async
    public void write(SysOperationLog log) {
        try {
            operationLogMapper.insert(log);
        } catch (Exception e) {
            // 日志写入失败绝不能影响业务，最多在后台日志里留下痕迹
            OperationLogWriter.log.warn("操作日志写入失败: {}", e.getMessage());
        }
    }
}