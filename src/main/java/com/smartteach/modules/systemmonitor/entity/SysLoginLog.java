package com.smartteach.modules.systemmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId
    private Long id;

    private String username;

    /** 登录IP */
    private String ip;

    /** 登录地点（可结合IP库解析，暂存占位） */
    private String location;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 登录状态 0失败 1成功 */
    private Integer status;

    /** 提示消息 */
    private String message;

    private LocalDateTime loginTime;
}
