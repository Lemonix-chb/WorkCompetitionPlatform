package com.example.workcompetitionplatform.security;

import com.example.workcompetitionplatform.config.JwtUtils;
import com.example.workcompetitionplatform.entity.User;
import com.example.workcompetitionplatform.util.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT Token，验证并设置SecurityContext
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中获取Authorization
            String authHeader = request.getHeader(jwtUtils.getTokenHeader());

            // 提取Token
            String token = jwtUtils.extractToken(authHeader);

            // 如果Token存在且SecurityContext中未设置认证信息
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 从Token中提取用户名
                String username = jwtUtils.getUsernameFromToken(token);

                // 如果用户名存在
                if (username != null) {
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 验证Token是否有效
                    if (jwtUtils.validateToken(token, userDetails)) {
                        // 验证用户状态 - 禁用用户不能通过JWT访问
                        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                            logger.warn("User account is disabled or locked: " + username);
                            // 不设置认证信息，让请求继续但保持未认证状态
                            filterChain.doFilter(request, response);
                            return;
                        }

                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // 设置认证详情
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // 将认证信息设置到SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 设置用户信息到UserContext（用于业务层获取当前用户）
                        // 从CustomUserDetails中获取完整的用户实体
                        if (userDetails instanceof CustomUserDetails) {
                            User user = ((CustomUserDetails) userDetails).getUser();
                            UserContext.setCurrentUser(user);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 记录错误日志但不抛出异常，让请求继续进行
            // 对于认证相关的异常，应该清除SecurityContext以确保安全
            logger.error("JWT authentication failed: " + e.getMessage(), e);
            SecurityContextHolder.clearContext();
            UserContext.clear();
        }

        try {
            // 继续过滤链
            filterChain.doFilter(request, response);
        } finally {
            // 清除UserContext，防止内存泄漏（无论请求成功或失败都要清理）
            UserContext.clear();
        }
    }
}