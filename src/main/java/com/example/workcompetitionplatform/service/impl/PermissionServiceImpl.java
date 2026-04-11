package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.Permission;
import com.example.workcompetitionplatform.mapper.PermissionMapper;
import com.example.workcompetitionplatform.service.IPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    @Override
    public Permission getByPermissionCode(String permissionCode) {
        return baseMapper.selectByPermissionCode(permissionCode);
    }

    @Override
    public List<Permission> listActivePermissions() {
        // Permission 表没有 status 字段，直接返回所有权限
        return list();
    }

    @Override
    public boolean existsByPermissionCode(String permissionCode) {
        return getByPermissionCode(permissionCode) != null;
    }
}