package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 团队邀请实体类
 * 对应数据库表：team_invitation
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("team_invitation")
public class TeamInvitation {

    /**
     * 邀请ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 邀请方团队ID
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 邀请人（队长）ID
     */
    @TableField(value = "inviter_id")
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    @TableField(value = "invitee_id")
    private Long inviteeId;

    /**
     * 被邀请人学号
     */
    @TableField(value = "invitee_student_no")
    private String inviteeStudentNo;

    /**
     * 被邀请人邮箱
     */
    @TableField(value = "invitee_email")
    private String inviteeEmail;

    /**
     * 邀请消息
     */
    @TableField(value = "message")
    private String message;

    /**
     * 邀请状态：PENDING-待处理, ACCEPTED-已接受, REJECTED-已拒绝, EXPIRED-已过期, CANCELLED-已取消
     */
    @TableField(value = "status")
    private InvitationStatus status = InvitationStatus.PENDING;

    /**
     * 过期时间（默认7天）
     */
    @TableField(value = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 处理时间
     */
    @TableField(value = "process_time")
    private LocalDateTime processTime;

    /**
     * 邀请状态枚举
     */
    public enum InvitationStatus {
        PENDING,     // 待处理
        ACCEPTED,    // 已接受
        REJECTED,    // 已拒绝
        EXPIRED,     // 已过期
        CANCELLED    // 已取消
    }

    // ===== 非数据库字段，用于前端显示 =====

    /**
     * 团队名称（非数据库字段）
     */
    @TableField(exist = false)
    private String teamName;

    /**
     * 邀请人姓名（非数据库字段）
     */
    @TableField(exist = false)
    private String inviterName;

    /**
     * 邀请人学号/工号（非数据库字段）
     */
    @TableField(exist = false)
    private String inviterNo;
}