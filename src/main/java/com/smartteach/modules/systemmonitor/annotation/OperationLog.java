package com.smartteach.modules.systemmonitor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务方法上加此注解可被 OperationLogAspect 拦截并自动写入 sys_operation_log
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 操作模块，如 "用户管理" */
    String module() default "";

    /** 操作描述，如 "新增用户" */
    String action() default "";

    /** 是否记录请求参数与返回结果 */
    boolean saveParams() default true;
}
