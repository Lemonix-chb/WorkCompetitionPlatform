package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报名实体类
 * 对应数据库表：registration
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("registration")
public class Registration {

    /**
     * 报名ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报名团队ID
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 所属赛事ID
     */
    @TableField(value = "competition_id")
    private Long competitionId;

    /**
     * 所属赛道ID
     */
    @TableField(value = "track_id")
    private Long trackId;

    /**
     * 报名编号
     */
    @TableField(value = "registration_code")
    private String registrationCode;

    /**
     * 提交报名的人（队长）ID
     */
    @TableField(value = "submitter_id")
    private Long submitterId;

    /**
     * 报名时间
     */
    @TableField(value = "registration_time")
    private LocalDateTime registrationTime;

    /**
     * 报名状态：DRAFT-草稿, PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝
     */
    @TableField(value = "status")
    private RegistrationStatus status = RegistrationStatus.DRAFT;

    /**
     * 审核意见
     */
    @TableField(value = "review_comment")
    private String reviewComment;

    /**
     * 审核人（管理员）ID
     */
    @TableField(value = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField(value = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 报名状态枚举
     */
    public enum RegistrationStatus {
        DRAFT,     // 草稿
        PENDING,   // 待审核
        APPROVED,  // 已通过
        REJECTED   // 已拒绝
    }
}