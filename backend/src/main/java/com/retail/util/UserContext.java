package com.retail.util;

/**
 * 线程本地用户上下文 - 存储当前请求的用户信息
 */
public class UserContext {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();
    private static final ThreadLocal<Long> storeIdHolder = new ThreadLocal<>();

    public static void setUserId(Long userId) { userIdHolder.set(userId); }
    public static Long getUserId() { return userIdHolder.get(); }

    public static void setUsername(String username) { usernameHolder.set(username); }
    public static String getUsername() { return usernameHolder.get(); }

    public static void setRole(String role) { roleHolder.set(role); }
    public static String getRole() { return roleHolder.get(); }

    public static void setStoreId(Long storeId) { storeIdHolder.set(storeId); }
    public static Long getStoreId() { return storeIdHolder.get(); }

    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return "admin".equals(getRole());
    }

    /**
     * 检查当前用户是否为店长
     */
    public static boolean isManager() {
        return "manager".equals(getRole());
    }

    /**
     * 清除线程变量（防止内存泄漏）
     */
    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
        roleHolder.remove();
        storeIdHolder.remove();
    }
}
