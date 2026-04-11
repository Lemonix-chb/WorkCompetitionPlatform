package com.example.workcompetitionplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.dto.PageResponse;
import com.example.workcompetitionplatform.entity.User;
import com.example.workcompetitionplatform.exception.BusinessException;
import com.example.workcompetitionplatform.mapper.UserMapper;
import com.example.workcompetitionplatform.service.IUserService;
import com.example.workcompetitionplatform.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 提供用户查询、更新、密码修改和状态更新的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询用户列表（分页）
     * 管理员可以查询所有用户，普通用户只能查询部分信息
     *
     * @param current 当前页码
     * @param size 每页记录数
     * @param keyword 搜索关键词（用户名、真实姓名、学号）
     * @param role 用户角色（STUDENT/JUDGE/ADMIN）
     * @param status 用户状态
     * @return API响应（包含分页用户列表）
     */
    @Operation(summary = "查询用户列表（分页）")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<User>> listUsers(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户角色") @RequestParam(required = false) String role,
            @Parameter(description = "用户状态") @RequestParam(required = false) User.UserStatus status) {

        // 如果指定了角色，使用Mapper的自定义查询方法
        if (role != null && !role.isEmpty()) {
            // 使用自定义的selectUsersByRole方法查询
            Page<User> page = userMapper.selectUsersByRole(
                    new Page<>(current, size),
                    role,
                    keyword,
                    status != null ? status.name() : null
            );

            // 构建分页响应
            PageResponse<User> pageResponse = PageResponse.of(
                    page.getRecords(),
                    page.getTotal(),
                    page.getCurrent(),
                    page.getSize()
            );

            return ApiResponse.success(pageResponse);
        }

        // 没有指定角色，使用通用查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 添加搜索关键词条件
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("username", keyword)
                    .or().like("real_name", keyword)
                    .or().like("student_no", keyword)
                    .or().like("teacher_no", keyword)
            );
        }

        // 添加状态条件
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc("create_time");

        // 执行分页查询
        Page<User> page = userService.page(new Page<>(current, size), queryWrapper);

        // 构建分页响应
        PageResponse<User> pageResponse = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );

        return ApiResponse.success(pageResponse);
    }

    /**
     * 查询用户详情
     * 根据用户ID查询用户详细信息
     *
     * @param id 用户ID
     * @return API响应（包含用户详情）
     */
    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ApiResponse<User> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);

        if (user == null) {
            return ApiResponse.notFound("用户不存在");
        }

        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息
     * 用户可以更新自己的基本信息，管理员可以更新所有用户信息
     *
     * @param id 用户ID
     * @param user 用户实体（包含更新信息）
     * @return API响应
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ApiResponse<Void> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody User user) {

        // 检查用户是否存在
        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return ApiResponse.notFound("用户不存在");
        }

        // 设置用户ID
        user.setId(id);

        // 非管理员只能更新自己的部分信息
        if (!UserContext.isAdmin()) {
            // 防止非管理员修改角色和状态
            user.setRole(null);
            user.setStatus(null);
        }

        // 更新用户信息
        boolean success = userService.updateById(user);

        if (!success) {
            return ApiResponse.error("更新失败");
        }

        log.info("用户信息更新成功：{}", id);

        return ApiResponse.success("更新成功");
    }

    /**
     * 修改密码
     * 用户修改自己的密码，需要提供旧密码和新密码
     *
     * @param id 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return API响应
     */
    @Operation(summary = "修改密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("#id == authentication.principal.id")
    public ApiResponse<Void> changePassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "旧密码") @RequestParam String oldPassword,
            @Parameter(description = "新密码") @RequestParam String newPassword) {

        try {
            // 执行密码修改
            boolean success = userService.changePassword(id, oldPassword, newPassword);

            if (!success) {
                return ApiResponse.error("密码修改失败");
            }

            log.info("用户密码修改成功：{}", id);

            return ApiResponse.success("密码修改成功");
        } catch (BusinessException e) {
            log.error("用户密码修改失败：{}", id, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态
     * 管理员可以更新用户状态（审核、激活、禁用）
     *
     * @param id 用户ID
     * @param status 用户状态
     * @return API响应
     */
    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "用户状态") @RequestParam User.UserStatus status) {

        // 检查用户是否存在
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.notFound("用户不存在");
        }

        // 更新用户状态
        boolean success = userService.updateStatus(id, status);

        if (!success) {
            return ApiResponse.error("状态更新失败");
        }

        log.info("用户状态更新成功：{} -> {}", id, status);

        return ApiResponse.success("状态更新成功");
    }

    /**
     * 搜索学生（供团队邀请使用）
     * 学生可以搜索其他学生用于邀请加入团队
     *
     * @param keyword 搜索关键词（学号或姓名）
     * @return API响应（包含学生列表）
     */
    @Operation(summary = "搜索学生（供团队邀请）")
    @GetMapping("/students/search")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<User>> searchStudents(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {

        // 使用已有的selectUsersByRole方法查询学生
        // 传入一个很大的Page对象来获取所有结果（不分页）
        Page<User> page = userMapper.selectUsersByRole(
                new Page<>(1, 1000), // 获取最多1000个结果
                "STUDENT",           // 角色代码
                keyword,             // 搜索关键词
                "ACTIVE"             // 只查询激活状态的用户
        );

        List<User> students = page.getRecords();

        return ApiResponse.success(students);
    }

    /**
     * 查询待审核用户列表
     * 管理员查询所有待审核的用户
     *
     * @return API响应（包含待审核用户列表）
     */
    @Operation(summary = "查询待审核用户列表")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<User>> listPendingUsers() {
        List<User> pendingUsers = userService.listPendingUsers();
        return ApiResponse.success(pendingUsers);
    }

    /**
     * 分配用户角色
     * 管理员给用户分配角色，同时激活用户账号
     *
     * @param id 用户ID
     * @param roleCode 角色代码（STUDENT/JUDGE/ADMIN）
     * @return API响应
     */
    @Operation(summary = "分配用户角色")
    @PostMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> assignRole(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "角色代码") @RequestParam String roleCode) {

        try {
            // 验证角色代码
            if (!roleCode.equals("STUDENT") && !roleCode.equals("JUDGE") && !roleCode.equals("ADMIN")) {
                return ApiResponse.badRequest("无效的角色代码，必须是 STUDENT、JUDGE 或 ADMIN");
            }

            // 分配角色
            boolean success = userService.assignRole(id, roleCode);

            if (!success) {
                return ApiResponse.error("角色分配失败");
            }

            log.info("角色分配成功：用户 {} -> 角色 {}", id, roleCode);

            return ApiResponse.success("角色分配成功，用户已激活");
        } catch (RuntimeException e) {
            log.error("角色分配失败：用户 {}", id, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户角色
     * 管理员修改用户的角色
     *
     * @param id 用户ID
     * @param roleCode 角色代码（STUDENT/JUDGE/ADMIN）
     * @return API响应
     */
    @Operation(summary = "更新用户角色")
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateRole(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "角色代码") @RequestParam String roleCode) {

        try {
            // 验证角色代码
            if (!roleCode.equals("STUDENT") && !roleCode.equals("JUDGE") && !roleCode.equals("ADMIN")) {
                return ApiResponse.badRequest("无效的角色代码，必须是 STUDENT、JUDGE 或 ADMIN");
            }

            // 更新角色
            boolean success = userService.updateRole(id, roleCode);

            if (!success) {
                return ApiResponse.error("角色更新失败");
            }

            log.info("角色更新成功：用户 {} -> 角色 {}", id, roleCode);

            return ApiResponse.success("角色更新成功");
        } catch (RuntimeException e) {
            log.error("角色更新失败：用户 {}", id, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 移除用户角色
     * 管理员移除用户的角色
     *
     * @param id 用户ID
     * @return API响应
     */
    @Operation(summary = "移除用户角色")
    @DeleteMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> removeRole(@Parameter(description = "用户ID") @PathVariable Long id) {

        try {
            // 移除角色
            boolean success = userService.removeRole(id);

            if (!success) {
                return ApiResponse.error("角色移除失败");
            }

            log.info("角色移除成功：用户 {}", id);

            return ApiResponse.success("角色移除成功");
        } catch (RuntimeException e) {
            log.error("角色移除失败：用户 {}", id, e);
            return ApiResponse.error(e.getMessage());
        }
    }
}