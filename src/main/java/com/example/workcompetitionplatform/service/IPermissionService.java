package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface IPermissionService extends IService<Permission> {

    /**
     * 根据权限编码查询权限
     *
     * @param permissionCode 权限编码
     * @return 权限实体
     */
    Permission getByPermissionCode(String permissionCode);

    /**
     * 查询所有激活状态的权限
     *
     * @return 权限列表
     */
    List<Permission> listActivePermissions();

    /**
     * 检查权限编码是否存在
     *
     * @param permissionCode 权限编码
     * @return 是否存在
     */
    boolean existsByPermissionCode(String permissionCode);
}