package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知消息实体类
 * 对应数据库表：notification
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification")
public class Notification {

    /**
     * 通知ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 通知标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 通知内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 通知类型：INVITE-邀请, APPLICATION-申请, REGISTRATION-报名, SUBMISSION-提交, REVIEW-评审, APPEAL-申诉, SYSTEM-系统
     */
    @TableField(value = "notification_type")
    private NotificationType notificationType;

    /**
     * 关联业务ID
     */
    @TableField(value = "related_id")
    private Long relatedId;

    /**
     * 关联业务类型
     */
    @TableField(value = "related_type")
    private String relatedType;

    /**
     * 是否已读
     */
    @TableField(value = "is_read")
    private Boolean isRead = false;

    /**
     * 阅读时间
     */
    @TableField(value = "read_time")
    private LocalDateTime readTime;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        INVITE,        // 邀请
        APPLICATION,   // 申请
        REGISTRATION,  // 报名
        SUBMISSION,    // 提交
        REVIEW,        // 评审
        APPEAL,        // 申诉
        SYSTEM         // 系统
    }
}