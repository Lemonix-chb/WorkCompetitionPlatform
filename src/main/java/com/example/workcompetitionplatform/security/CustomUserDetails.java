package com.example.workcompetitionplatform.security;

import com.example.workcompetitionplatform.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 包含完整用户信息的UserDetails实现
 * 用于在SecurityContext中存储用户详情，同时提供完整的User实体访问
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Collection<GrantedAuthority> authorities;

    public CustomUserDetails(User user, Collection<GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    /**
     * 获取完整的用户实体
     *
     * @return User实体
     */
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() == User.UserStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == User.UserStatus.ACTIVE;
    }
}