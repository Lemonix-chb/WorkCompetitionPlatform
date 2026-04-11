package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface INotificationService extends IService<Notification> {

    /**
     * 发送通知
     *
     * @param recipientUserId 接收用户ID
     * @param notificationType 通知类型
     * @param title 通知标题
     * @param content 通知内容
     * @return 通知实体
     */
    Notification sendNotification(Long recipientUserId, Notification.NotificationType notificationType,
                                   String title, String content);

    /**
     * 根据接收用户ID查询通知列表
     *
     * @param recipientUserId 接收用户ID
     * @return 通知列表
     */
    List<Notification> listByRecipientUserId(Long recipientUserId);

    /**
     * 查询用户的未读通知列表
     *
     * @param recipientUserId 接收用户ID
     * @return 未读通知列表
     */
    List<Notification> listUnreadNotifications(Long recipientUserId);

    /**
     * 查询用户的已读通知列表
     *
     * @param recipientUserId 接收用户ID
     * @return 已读通知列表
     */
    List<Notification> listReadNotifications(Long recipientUserId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param recipientUserId 接收用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long notificationId, Long recipientUserId);

    /**
     * 批量标记通知为已读
     *
     * @param recipientUserId 接收用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long recipientUserId);

    /**
     * 获取用户未读通知数量
     *
     * @param recipientUserId 接收用户ID
     * @return 未读通知数量
     */
    int countUnreadNotifications(Long recipientUserId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param recipientUserId 接收用户ID
     * @return 是否成功
     */
    boolean deleteNotification(Long notificationId, Long recipientUserId);
}