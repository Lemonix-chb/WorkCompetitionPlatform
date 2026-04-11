package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查询所有角色关联
     *
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询所有用户关联
     *
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    List<UserRole> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除用户的所有角色关联
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除角色的所有用户关联
     *
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
}