package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID查询所有权限关联
     *
     * @param roleId 角色ID
     * @return 角色权限关联列表
     */
    List<RolePermission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID查询所有角色关联
     *
     * @param permissionId 权限ID
     * @return 角色权限关联列表
     */
    List<RolePermission> selectByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 删除角色的所有权限关联
     *
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除权限的所有角色关联
     *
     * @param permissionId 权限ID
     * @return 删除的记录数
     */
    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}