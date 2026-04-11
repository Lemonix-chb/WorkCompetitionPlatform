package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：user
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {

    /**
     * 用户ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（学号/工号）
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    @TableField(value = "password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 学院
     */
    @TableField(value = "college")
    private String college;

    /**
     * 专业
     */
    @TableField(value = "major")
    private String major;

    /**
     * 学号
     */
    @TableField(value = "student_no")
    private String studentNo;

    /**
     * 工号（教师工号）
     */
    @TableField(value = "teacher_no")
    private String teacherNo;

    /**
     * 用户角色：STUDENT-学生, JUDGE-评委, ADMIN-管理员
     */
    @TableField(exist = false)  // 不映射到数据库，用于简化查询
    private UserRole role;

    /**
     * 账号状态：ACTIVE-正常, DISABLED-禁用, PENDING-待审核
     */
    @TableField(value = "status")
    private UserStatus status = UserStatus.ACTIVE;

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
     * 用户角色枚举
     */
    public enum UserRole {
        STUDENT,   // 学生
        JUDGE,     // 评委
        ADMIN      // 管理员
    }

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE,    // 正常
        DISABLED,  // 禁用
        PENDING    // 待审核
    }
}