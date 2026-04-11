package com.example.workcompetitionplatform.service;

import com.example.workcompetitionplatform.entity.User;
import com.example.workcompetitionplatform.mapper.UserMapper;
import com.example.workcompetitionplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security用户详情服务实现类
 * 用于加载用户认证信息和权限
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户名加载用户详情
     * 支持使用用户名、学号或工号登录
     *
     * @param username 用户名（可以是username、studentNo或teacherNo）
     * @return UserDetails实例
     * @throws UsernameNotFoundException 用户未找到异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 尝试根据用户名查询用户
        User user = userMapper.selectByUsername(username);

        // 如果用户名未找到，尝试根据学号查询
        if (user == null) {
            user = userMapper.selectByStudentNo(username);
        }

        // 如果学号也未找到，尝试根据工号查询（教师登录）
        if (user == null) {
            user = userMapper.selectByTeacherNo(username);
        }

        // 如果工号也未找到，尝试根据邮箱查询
        if (user == null) {
            user = userMapper.selectByEmail(username);
        }

        // 如果仍未找到用户，抛出异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 检查用户状态
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("用户状态异常：" + user.getStatus());
        }

        // 构建权限列表
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 添加角色权限（ROLE_前缀）
        String roleName = "ROLE_" + user.getRole().name();
        authorities.add(new SimpleGrantedAuthority(roleName));

        // 返回自定义的UserDetails，包含完整用户信息
        return new CustomUserDetails(user, authorities);
    }

    /**
     * 根据用户ID加载用户详情
     *
     * @param userId 用户ID
     * @return UserDetails实例
     * @throws UsernameNotFoundException 用户未找到异常
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在，ID：" + userId);
        }

        // 检查用户状态
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("用户状态异常：" + user.getStatus());
        }

        // 构建权限列表
        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = "ROLE_" + user.getRole().name();
        authorities.add(new SimpleGrantedAuthority(roleName));

        return new CustomUserDetails(user, authorities);
    }
}