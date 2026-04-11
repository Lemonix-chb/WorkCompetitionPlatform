package com.example.workcompetitionplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限实体类
 * 对应数据库表：permission
 * 使用MyBatis Plus注解
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("permission")
public class Permission {

    /**
     * 权限ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称
     */
    @TableField(value = "permission_name")
    private String permissionName;

    /**
     * 权限编码
     */
    @TableField(value = "permission_code")
    private String permissionCode;

    /**
     * 资源类型：MENU-菜单, BUTTON-按钮, API-接口
     */
    @TableField(value = "resource_type")
    private ResourceType resourceType;

    /**
     * 资源路径
     */
    @TableField(value = "resource_url")
    private String resourceUrl;

    /**
     * 权限描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        MENU,    // 菜单
        BUTTON,  // 按钮
        API      // 接口
    }
}