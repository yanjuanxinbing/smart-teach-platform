package com.smartteach.common.result;

/**
 * 统一响应状态码
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "暂未登录或token已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "请求资源不存在"),

    // 用户/权限相关 1000+
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    USER_EXIST(1004, "用户已存在"),
    OLD_PASSWORD_ERROR(1006, "原密码不正确"),

    // 业务模块相关 2000+
    DATA_NOT_EXIST(2001, "数据不存在"),
    DATA_EXIST(2002, "数据已存在"),
    FILE_UPLOAD_ERROR(2003, "文件上传失败"),
    FILE_NOT_EXIST(2004, "文件不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
