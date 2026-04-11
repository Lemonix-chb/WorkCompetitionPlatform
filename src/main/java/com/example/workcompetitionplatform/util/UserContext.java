package com.example.workcompetitionplatform.util;

import com.example.workcompetitionplatform.entity.User;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户信息，基于ThreadLocal实现
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public class UserContext {

    /**
     * ThreadLocal存储当前用户信息
     */
    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户到ThreadLocal
     *
     * @param user 用户实体
     */
    public static void setCurrentUser(User user) {
        userThreadLocal.set(user);
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前用户实体，未登录则返回null
     */
    public static User getCurrentUser() {
        return userThreadLocal.get();
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 当前用户ID，未登录则返回null
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 当前用户名，未登录则返回null
     */
    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前登录用户真实姓名
     *
     * @return 当前用户真实姓名，未登录则返回null
     */
    public static String getCurrentRealName() {
        User user = getCurrentUser();
        return user != null ? user.getRealName() : null;
    }

    /**
     * 获取当前登录用户角色
     *
     * @return 当前用户角色，未登录则返回null
     */
    public static User.UserRole getCurrentUserRole() {
        User user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * 检查当前用户是否是管理员
     *
     * @return 是否是管理员
     */
    public static boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && user.getRole() == User.UserRole.ADMIN;
    }

    /**
     * 检查当前用户是否是教师
     *
     * @return 是否是教师
     */
    public static boolean isTeacher() {
        User user = getCurrentUser();
        return user != null && user.getRole() == User.UserRole.JUDGE;  // TEACHER 在系统中对应 JUDGE 角色
    }

    /**
     * 检查当前用户是否是学生
     *
     * @return 是否是学生
     */
    public static boolean isStudent() {
        User user = getCurrentUser();
        return user != null && user.getRole() == User.UserRole.STUDENT;
    }

    /**
     * 清除ThreadLocal中的用户信息
     * 在请求结束时必须调用，防止内存泄漏
     */
    public static void clear() {
        userThreadLocal.remove();
    }
}