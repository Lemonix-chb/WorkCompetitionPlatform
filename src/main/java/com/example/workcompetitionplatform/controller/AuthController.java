package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.config.JwtUtils;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.dto.LoginRequest;
import com.example.workcompetitionplatform.dto.RegisterRequest;
import com.example.workcompetitionplatform.entity.User;
import com.example.workcompetitionplatform.security.CustomUserDetails;
import com.example.workcompetitionplatform.service.IUserService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 提供用户登录、注册、登出和获取当前用户信息的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 用户登录
     * 验证用户凭证并返回JWT Token
     *
     * @param loginRequest 登录请求
     * @return API响应（包含Token和用户信息）
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 设置认证信息到SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 生成JWT Token
            String token = jwtUtils.generateToken(userDetails);

            // 从CustomUserDetails获取完整用户信息
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            User user = customUserDetails.getUser();

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenType", "Bearer");
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("role", user.getRole());
            data.put("email", user.getEmail());

            log.info("用户登录成功：{}", user.getUsername());

            return ApiResponse.success("登录成功", data);
        } catch (Exception e) {
            log.error("用户登录失败：{}", loginRequest.getUsername(), e);
            return ApiResponse.unauthorized("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     * 创建新用户账户
     *
     * @param registerRequest 注册请求
     * @return API响应（包含用户信息）
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // 执行注册
            User user = userService.register(registerRequest);

            log.info("用户注册成功：{}", registerRequest.getUsername());

            return ApiResponse.success("注册成功，请等待管理员审核", user);
        } catch (Exception e) {
            log.error("用户注册失败：{}", registerRequest.getUsername(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     * 清除SecurityContext中的认证信息
     *
     * @return API响应
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> logout() {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            log.info("用户登出：{}", username);
        }

        // 清除SecurityContext
        SecurityContextHolder.clearContext();

        return ApiResponse.success("登出成功");
    }

    /**
     * 获取当前用户信息
     * 返回当前登录用户的详细信息
     *
     * @return API响应（包含用户信息）
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<User> getCurrentUserInfo() {
        // 从SecurityContext获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询用户详细信息
        User user = userService.getByUsername(username);

        if (user == null) {
            return ApiResponse.notFound("用户不存在");
        }

        return ApiResponse.success(user);
    }

    /**
     * 刷新Token
     * 使用现有Token生成新的Token
     *
     * @param authorization 请求头Authorization
     * @return API响应（包含新Token）
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh-token")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String authorization) {
        try {
            // 提取Token
            String oldToken = jwtUtils.extractToken(authorization);

            if (oldToken == null) {
                return ApiResponse.badRequest("Token格式错误");
            }

            // 判断是否可以刷新
            if (!jwtUtils.canTokenBeRefreshed(oldToken)) {
                return ApiResponse.error("Token已过期，无法刷新");
            }

            // 刷新Token
            String newToken = jwtUtils.refreshToken(oldToken);

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);
            data.put("tokenType", "Bearer");

            log.info("Token刷新成功");

            return ApiResponse.success("Token刷新成功", data);
        } catch (JwtException e) {
            log.error("Token刷新失败", e);
            return ApiResponse.unauthorized("Token无效");
        }
    }
}