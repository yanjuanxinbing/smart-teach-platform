package com.smartteach.common.utils;

/**
 * 基于 ThreadLocal 保存当前请求的登录用户信息，供 MyBatis-Plus 自动填充、
 * 业务代码获取"当前操作人"等场景使用。由 JwtAuthenticationFilter 在鉴权通过后写入。
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
