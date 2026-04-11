package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface IRoleService extends IService<Role> {

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色实体
     */
    Role getByRoleCode(String roleCode);

    /**
     * 根据角色名称查询角色
     *
     * @param roleName 角色名称
     * @return 角色实体
     */
    Role getByRoleName(String roleName);

    /**
     * 查询所有激活状态的角色
     *
     * @return 角色列表
     */
    List<Role> listActiveRoles();

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @return 是否存在
     */
    boolean existsByRoleCode(String roleCode);
}