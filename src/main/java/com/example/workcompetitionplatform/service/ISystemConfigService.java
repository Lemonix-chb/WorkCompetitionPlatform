package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.SystemConfig;

import java.util.List;

/**
 * 系统配置服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface ISystemConfigService extends IService<SystemConfig> {

    /**
     * 根据配置键查询配置项
     *
     * @param configKey 配置键
     * @return 系统配置实体
     */
    SystemConfig getByConfigKey(String configKey);

    /**
     * 根据配置键查询配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置分组查询配置列表
     *
     * @param configGroup 配置分组
     * @return 系统配置列表
     */
    List<SystemConfig> listByConfigGroup(String configGroup);

    /**
     * 更新配置值
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否成功
     */
    boolean updateConfigValue(String configKey, String configValue);

    /**
     * 检查配置键是否存在
     *
     * @param configKey 配置键
     * @return 是否存在
     */
    boolean existsByConfigKey(String configKey);
}