package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 根据接收用户ID查询通知列表
     *
     * @param recipientUserId 接收用户ID
     * @return 通知列表
     */
    List<Notification> selectByRecipientUserId(@Param("recipientUserId") Long recipientUserId);

    /**
     * 根据通知类型查询通知列表
     *
     * @param notificationType 通知类型
     * @return 通知列表
     */
    List<Notification> selectByNotificationType(@Param("notificationType") Notification.NotificationType notificationType);

    /**
     * 根据是否已读查询通知列表
     *
     * @param isRead 是否已读
     * @return 通知列表
     */
    List<Notification> selectByIsRead(@Param("isRead") Boolean isRead);

    /**
     * 根据接收用户ID查询未读通知数量
     *
     * @param recipientUserId 接收用户ID
     * @return 未读通知数量
     */
    int countUnreadByRecipientUserId(@Param("recipientUserId") Long recipientUserId);

    /**
     * 根据接收用户ID和通知类型查询通知列表
     *
     * @param recipientUserId 接收用户ID
     * @param notificationType 通知类型
     * @return 通知列表
     */
    List<Notification> selectByRecipientUserIdAndType(@Param("recipientUserId") Long recipientUserId, @Param("notificationType") Notification.NotificationType notificationType);
}