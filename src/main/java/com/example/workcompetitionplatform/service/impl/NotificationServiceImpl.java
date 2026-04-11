package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.Notification;
import com.example.workcompetitionplatform.mapper.NotificationMapper;
import com.example.workcompetitionplatform.service.INotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification sendNotification(Long recipientUserId, Notification.NotificationType notificationType,
                                          String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(recipientUserId);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());

        save(notification);

        return notification;
    }

    @Override
    public List<Notification> listByRecipientUserId(Long recipientUserId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, recipientUserId)
                .orderByDesc(Notification::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Notification> listUnreadNotifications(Long recipientUserId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, recipientUserId)
                .eq(Notification::getIsRead, false)
                .orderByDesc(Notification::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Notification> listReadNotifications(Long recipientUserId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, recipientUserId)
                .eq(Notification::getIsRead, true)
                .orderByDesc(Notification::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long notificationId, Long recipientUserId) {
        Notification notification = getById(notificationId);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }

        if (!notification.getUserId().equals(recipientUserId)) {
            throw new RuntimeException("无权操作此通知");
        }

        notification.setIsRead(true);
        notification.setReadTime(LocalDateTime.now());

        return updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long recipientUserId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, recipientUserId)
                .eq(Notification::getIsRead, false);

        List<Notification> notifications = list(wrapper);
        LocalDateTime readTime = LocalDateTime.now();

        notifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadTime(readTime);
        });

        return updateBatchById(notifications);
    }

    @Override
    public int countUnreadNotifications(Long recipientUserId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, recipientUserId)
                .eq(Notification::getIsRead, false);
        return (int) count(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotification(Long notificationId, Long recipientUserId) {
        Notification notification = getById(notificationId);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }

        if (!notification.getUserId().equals(recipientUserId)) {
            throw new RuntimeException("无权删除此通知");
        }

        return removeById(notificationId);
    }
}