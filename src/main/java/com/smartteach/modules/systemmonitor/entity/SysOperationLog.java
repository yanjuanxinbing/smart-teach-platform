package com.smartteach.modules.systemmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId
    private Long id;

    /** 操作模块 */
    private String module;

    /** 操作描述 */
    private String action;

    /** 请求方法（HTTP方法+路径） */
    private String method;

    /** 请求URI */
    private String requestUri;

    /** HTTP方法 */
    private String httpMethod;

    /** 请求参数 */
    private String params;

    /** 返回结果（截取前2000字符） */
    private String result;

    /** 操作IP */
    private String ip;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 状态 0失败 1成功 */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long costTime;

    private LocalDateTime operationTime;
}
