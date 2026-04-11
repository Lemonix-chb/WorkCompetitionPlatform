package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.Role;
import com.example.workcompetitionplatform.entity.User;
import com.example.workcompetitionplatform.entity.UserRole;
import com.example.workcompetitionplatform.mapper.RoleMapper;
import com.example.workcompetitionplatform.mapper.UserMapper;
import com.example.workcompetitionplatform.mapper.UserRoleMapper;
import com.example.workcompetitionplatform.service.IUserService;
import com.example.workcompetitionplatform.dto.LoginRequest;
import com.example.workcompetitionplatform.dto.RegisterRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRoleMapper userRoleMapper, RoleMapper roleMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public User login(LoginRequest loginRequest) {
        // 查询用户
        User user = getByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("账号已被禁用或待审核");
        }

        return user;
    }

    @Override
    public User register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 检查学号是否已存在（如果有学号）
        if (registerRequest.getStudentNo() != null && existsByStudentNo(registerRequest.getStudentNo())) {
            throw new RuntimeException("学号已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRealName(registerRequest.getRealName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setCollege(registerRequest.getCollege());
        user.setMajor(registerRequest.getMajor());
        user.setStudentNo(registerRequest.getStudentNo());
        user.setStatus(User.UserStatus.PENDING); // 默认待审核状态

        // 保存用户
        save(user);

        return user;
    }

    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public User getByEmail(String email) {
        return baseMapper.selectByEmail(email);
    }

    @Override
    public User getByStudentNo(String studentNo) {
        return baseMapper.selectByStudentNo(studentNo);
    }

    @Override
    public List<User> listActiveUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, User.UserStatus.ACTIVE);
        return list(wrapper);
    }

    @Override
    public List<User> listPendingUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, User.UserStatus.PENDING);
        return list(wrapper);
    }

    @Override
    public boolean updateStatus(Long userId, User.UserStatus status) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        user.setStatus(status);
        return updateById(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return getByUsername(username) != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return getByEmail(email) != null;
    }

    @Override
    public boolean existsByStudentNo(String studentNo) {
        return studentNo != null && getByStudentNo(studentNo) != null;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRole(Long userId, String roleCode) {
        // 检查用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectByRoleCode(roleCode);
        if (role == null) {
            throw new RuntimeException("角色不存在: " + roleCode);
        }

        // 检查用户是否已有角色
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        if (userRoleMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户已分配角色，请使用更新角色功能");
        }

        // 分配角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);

        // 更新用户状态为ACTIVE
        user.setStatus(User.UserStatus.ACTIVE);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Long userId, String roleCode) {
        // 检查用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectByRoleCode(roleCode);
        if (role == null) {
            throw new RuntimeException("角色不存在: " + roleCode);
        }

        // 删除旧的角色关联
        userRoleMapper.deleteByUserId(userId);

        // 创建新的角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeRole(Long userId) {
        // 检查用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 删除用户的角色关联
        int deleted = userRoleMapper.deleteByUserId(userId);

        return deleted > 0;
    }
}