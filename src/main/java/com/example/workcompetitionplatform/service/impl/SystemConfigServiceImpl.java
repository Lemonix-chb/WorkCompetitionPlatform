package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.SystemConfig;
import com.example.workcompetitionplatform.mapper.SystemConfigMapper;
import com.example.workcompetitionplatform.service.ISystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统配置服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements ISystemConfigService {

    @Override
    public SystemConfig getByConfigKey(String configKey) {
        return baseMapper.selectByConfigKey(configKey);
    }

    @Override
    public String getConfigValue(String configKey) {
        SystemConfig config = getByConfigKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public List<SystemConfig> listByConfigGroup(String configGroup) {
        return baseMapper.selectByConfigGroup(configGroup);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfigValue(String configKey, String configValue) {
        SystemConfig config = getByConfigKey(configKey);
        if (config == null) {
            throw new RuntimeException("配置项不存在");
        }

        config.setConfigValue(configValue);
        return updateById(config);
    }

    @Override
    public boolean existsByConfigKey(String configKey) {
        return getByConfigKey(configKey) != null;
    }
}