package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.entity.Notification;
import com.example.workcompetitionplatform.exception.BusinessException;
import com.example.workcompetitionplatform.service.INotificationService;
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
 * 通知管理控制器
 * 提供通知查询、标记已读和删除的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    /**
     * 查询通知列表
     * 查询当前用户的所有通知列表（包括已读和未读）
     *
     * @return API响应（包含通知列表）
     */
    @Operation(summary ="查询通知列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Notification>> listNotifications() {
        Long userId = UserContext.getCurrentUserId();
        List<Notification> notifications = notificationService.listByRecipientUserId(userId);
        return ApiResponse.success(notifications);
    }

    /**
     * 查询未读通知列表
     * 查询当前用户的未读通知列表
     *
     * @return API响应（包含未读通知列表）
     */
    @Operation(summary ="查询未读通知列表")
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Notification>> listUnreadNotifications() {
        Long userId = UserContext.getCurrentUserId();
        List<Notification> notifications = notificationService.listUnreadNotifications(userId);
        return ApiResponse.success(notifications);
    }

    /**
     * 查询已读通知列表
     * 查询当前用户的已读通知列表
     *
     * @return API响应（包含已读通知列表）
     */
    @Operation(summary ="查询已读通知列表")
    @GetMapping("/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Notification>> listReadNotifications() {
        Long userId = UserContext.getCurrentUserId();
        List<Notification> notifications = notificationService.listReadNotifications(userId);
        return ApiResponse.success(notifications);
    }

    /**
     * 查询未读通知数量
     * 统计当前用户的未读通知数量
     *
     * @return API响应
     */
    @Operation(summary ="查询未读通知数量")
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Integer> countUnreadNotifications() {
        Long userId = UserContext.getCurrentUserId();
        int count = notificationService.countUnreadNotifications(userId);
        return ApiResponse.success(count);
    }

    /**
     * 标记通知已读
     * 将指定通知标记为已读
     *
     * @param id 通知ID
     * @return API响应
     */
    @Operation(summary ="标记通知已读")
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAsRead(@Parameter(description ="通知ID") @PathVariable Long id) {
        try {
            Long userId = UserContext.getCurrentUserId();

            // 标记已读
            boolean success = notificationService.markAsRead(id, userId);

            if (!success) {
                return ApiResponse.error("标记已读失败");
            }

            log.info("通知标记已读：{}", id);

            return ApiResponse.success("已标记为已读");
        } catch (BusinessException e) {
            log.error("标记通知已读失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 全部标记已读
     * 将当前用户的所有未读通知标记为已读
     *
     * @return API响应
     */
    @Operation(summary ="全部标记已读")
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAllAsRead() {
        try {
            Long userId = UserContext.getCurrentUserId();

            // 全部标记已读
            boolean success = notificationService.markAllAsRead(userId);

            if (!success) {
                return ApiResponse.error("全部标记已读失败");
            }

            log.info("用户 {} 所有通知已标记为已读", userId);

            return ApiResponse.success("所有通知已标记为已读");
        } catch (BusinessException e) {
            log.error("全部标记已读失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除通知
     * 删除指定的通知
     *
     * @param id 通知ID
     * @return API响应
     */
    @Operation(summary ="删除通知")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteNotification(@Parameter(description ="通知ID") @PathVariable Long id) {
        try {
            Long userId = UserContext.getCurrentUserId();

            // 删除通知
            boolean success = notificationService.deleteNotification(id, userId);

            if (!success) {
                return ApiResponse.error("删除失败");
            }

            log.info("通知删除成功：{}", id);

            return ApiResponse.success("通知已删除");
        } catch (BusinessException e) {
            log.error("删除通知失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}