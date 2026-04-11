package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 团队申请实体类
 * 对应数据库表：team_application
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("team_application")
public class TeamApplication {

    /**
     * 申请ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请加入的团队ID
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 申请人ID
     */
    @TableField(value = "applicant_id")
    private Long applicantId;

    /**
     * 申请人学号
     */
    @TableField(value = "applicant_student_no")
    private String applicantStudentNo;

    /**
     * 申请人邮箱
     */
    @TableField(value = "applicant_email")
    private String applicantEmail;

    /**
     * 申请消息
     */
    @TableField(value = "message")
    private String message;

    /**
     * 申请状态：PENDING-待处理, ACCEPTED-已接受, REJECTED-已拒绝, CANCELLED-已取消
     */
    @TableField(value = "status")
    private ApplicationStatus status = ApplicationStatus.PENDING;

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
     * 处理人（队长）ID
     */
    @TableField(value = "processor_id")
    private Long processorId;

    /**
     * 申请人姓名（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String applicantName;

    /**
     * 团队名称（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String teamName;

    /**
     * 申请状态枚举
     */
    public enum ApplicationStatus {
        PENDING,     // 待处理
        ACCEPTED,    // 已接受
        REJECTED,    // 已拒绝
        CANCELLED    // 已取消
    }
}