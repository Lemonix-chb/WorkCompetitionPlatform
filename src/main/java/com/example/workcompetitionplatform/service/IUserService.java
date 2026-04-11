package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.User;
import com.example.workcompetitionplatform.dto.LoginRequest;
import com.example.workcompetitionplatform.dto.RegisterRequest;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface IUserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录成功的用户信息
     */
    User login(LoginRequest loginRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册成功的用户信息
     */
    User register(RegisterRequest registerRequest);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    User getByEmail(String email);

    /**
     * 根据学号查询用户
     *
     * @param studentNo 学号
     * @return 用户实体
     */
    User getByStudentNo(String studentNo);

    /**
     * 查询所有正常用户
     *
     * @return 用户列表
     */
    List<User> listActiveUsers();

    /**
     * 查询所有待审核用户
     *
     * @return 用户列表
     */
    List<User> listPendingUsers();

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 用户状态
     * @return 是否成功
     */
    boolean updateStatus(Long userId, User.UserStatus status);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查学号是否存在
     *
     * @param studentNo 学号
     * @return 是否存在
     */
    boolean existsByStudentNo(String studentNo);

    /**
     * 用户修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 分配用户角色
     * 管理员给用户分配角色，同时更新用户状态为ACTIVE
     *
     * @param userId 用户ID
     * @param roleCode 角色代码（STUDENT/JUDGE/ADMIN）
     * @return 是否成功
     */
    boolean assignRole(Long userId, String roleCode);

    /**
     * 更新用户角色
     * 管理员修改用户的角色
     *
     * @param userId 用户ID
     * @param roleCode 角色代码（STUDENT/JUDGE/ADMIN）
     * @return 是否成功
     */
    boolean updateRole(Long userId, String roleCode);

    /**
     * 移除用户角色
     * 管理员移除用户的角色
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean removeRole(Long userId);
}