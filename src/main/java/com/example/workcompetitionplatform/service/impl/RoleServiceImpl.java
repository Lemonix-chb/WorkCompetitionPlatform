package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.Role;
import com.example.workcompetitionplatform.mapper.RoleMapper;
import com.example.workcompetitionplatform.service.IRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Override
    public Role getByRoleCode(String roleCode) {
        return baseMapper.selectByRoleCode(roleCode);
    }

    @Override
    public Role getByRoleName(String roleName) {
        return baseMapper.selectByRoleName(roleName);
    }

    @Override
    public List<Role> listActiveRoles() {
        // Role 表没有 status 字段，直接返回所有角色
        return list();
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        return getByRoleCode(roleCode) != null;
    }
}