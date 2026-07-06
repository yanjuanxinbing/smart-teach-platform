package com.smartteach.modules.systemmonitor.aspect;

import com.alibaba.fastjson2.JSON;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import com.smartteach.modules.systemmonitor.entity.SysOperationLog;
import com.smartteach.modules.systemmonitor.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面：拦截 @OperationLog 注解，将调用信息写入数据库
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogMapper operationLogMapper;

    @Pointcut("@annotation(com.smartteach.modules.systemmonitor.annotation.OperationLog)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long cost = System.currentTimeMillis() - start;
            try {
                saveLog(pjp, result, error, cost);
            } catch (Exception e) {
                log.warn("操作日志写入失败: {}", e.getMessage());
            }
        }
    }

    @Async
    public void saveLog(ProceedingJoinPoint pjp, Object result, Throwable error, long cost) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        if (annotation == null) return;

        SysOperationLog log = new SysOperationLog();
        log.setModule(annotation.module());
        log.setAction(annotation.action());
        log.setMethod(method.getDeclaringClass().getName() + "." + method.getName());
        log.setCostTime(cost);
        log.setUserId(UserContext.getUserId());
        log.setUsername(UserContext.getUsername());

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            log.setRequestUri(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
            log.setIp(getClientIp(request));
        }
        if (annotation.saveParams()) {
            log.setParams(safeJson(pjp.getArgs()));
            log.setResult(safeJson(result));
        }
        if (error != null) {
            log.setStatus(0);
            log.setErrorMsg(error.getMessage());
        } else {
            log.setStatus(1);
        }
        log.setOperationTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private String safeJson(Object obj) {
        if (obj == null) return null;
        try {
            String s = JSON.toJSONString(obj);
            return s.length() > 2000 ? s.substring(0, 2000) : s;
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
