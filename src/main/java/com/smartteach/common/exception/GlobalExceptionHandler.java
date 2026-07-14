package com.smartteach.common.exception;

import com.smartteach.common.result.Result;
import com.smartteach.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 全局异常处理，统一返回 Result 格式，避免异常堆栈直接暴露给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 匹配 hasAuthority('xxx') / hasAuthority("xxx") —— 同时兼容 OR/AND 复合表达式 */
    private static final Pattern HAS_AUTHORITY_PATTERN =
            Pattern.compile("hasAuthority\\s*\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)");

    private final RequestMappingHandlerMapping handlerMapping;

    public GlobalExceptionHandler(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 权限不足：把当前接口 @PreAuthorize 里要求的权限标识提取出来，带回前端。
     * Spring Security 5.7 的 AccessDeniedException 本身不带具体权限，需要
     * 通过 HandlerMapping 反查请求 → controller 方法 → @PreAuthorize 注解。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        String required = extractRequiredAuthority();
        String msg = required != null
                ? "没有相关权限，需要: " + required
                : "没有相关权限";
        log.warn("权限不足: {} (required={})", msg, required);
        return Result.fail(ResultCode.FORBIDDEN.getCode(), msg);
    }

    /**
     * DB 唯一约束冲突兜底：并发场景下应用层 count 已放行，但 INSERT/UPDATE
     * 提交时 DB 抛 DuplicateKey，将原始异常翻译成"数据重复"友好提示。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("唯一约束冲突: {}", e.getMostSpecificCause().getMessage());
        return Result.fail(ResultCode.FAIL.getCode(), "数据已存在，请勿重复提交");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        // 调试期临时把根因返回给前端，便于定位；定位完成后改回 "系统繁忙，请稍后重试"
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return Result.fail(ResultCode.FAIL.getCode(), "系统繁忙：" + msg);
    }

    /**
     * 从当前请求反查 controller 方法的 @PreAuthorize 注解，正则提取所有
     * hasAuthority('xxx') 出现的权限标识，逗号拼接返回。失败返回 null。
     */
    private String extractRequiredAuthority() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            HandlerExecutionChain chain = handlerMapping.getHandler(req);
            if (chain == null) return null;
            Object handler = chain.getHandler();
            if (!(handler instanceof HandlerMethod)) return null;
            HandlerMethod hm = (HandlerMethod) handler;
            // 优先方法级，回退到类级
            PreAuthorize preAuth = hm.getMethodAnnotation(PreAuthorize.class);
            if (preAuth == null) {
                Method method = hm.getMethod();
                preAuth = method.getDeclaringClass().getAnnotation(PreAuthorize.class);
            }
            if (preAuth == null) return null;
            String expr = preAuth.value();
            Matcher m = HAS_AUTHORITY_PATTERN.matcher(expr);
            List<String> auths = new ArrayList<>(2);
            while (m.find()) {
                auths.add(m.group(1));
            }
            if (auths.isEmpty()) return null;
            return String.join(", ", auths);
        } catch (Exception ex) {
            log.debug("无法提取所需权限: {}", ex.getMessage());
            return null;
        }
    }
}