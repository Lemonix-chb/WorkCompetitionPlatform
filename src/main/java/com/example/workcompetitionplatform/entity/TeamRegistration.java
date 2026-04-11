package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 团队成员报名关联实体类
 * 对应数据库表：team_registration
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("team_registration")
public class TeamRegistration {

    /**
     * 关联ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 团队报名记录ID
     */
    @TableField(value = "registration_id")
    private Long registrationId;

    /**
     * 团队ID
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 队员用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 队员角色：LEADER-队长, MEMBER-队员
     */
    @TableField(value = "member_role")
    private MemberRole memberRole;

    /**
     * 学号
     */
    @TableField(value = "student_no")
    private String studentNo;

    /**
     * 姓名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 专业
     */
    @TableField(value = "major")
    private String major;

    /**
     * 学院
     */
    @TableField(value = "college")
    private String college;

    /**
     * 报名状态：AUTO_REGISTERED-自动报名, CONFIRMED-已确认, CANCELLED-已取消
     */
    @TableField(value = "registration_status")
    private TeamRegistrationStatus registrationStatus = TeamRegistrationStatus.AUTO_REGISTERED;

    /**
     * 报名时间（队长提交时自动填充）
     */
    @TableField(value = "register_time")
    private LocalDateTime registerTime;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 队员角色枚举
     */
    public enum MemberRole {
        LEADER,   // 队长
        MEMBER    // 队员
    }

    /**
     * 报名状态枚举
     */
    public enum TeamRegistrationStatus {
        AUTO_REGISTERED,  // 自动报名
        CONFIRMED,        // 已确认
        CANCELLED         // 已取消
    }
}