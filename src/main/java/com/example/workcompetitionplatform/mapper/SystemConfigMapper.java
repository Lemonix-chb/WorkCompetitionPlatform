package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查询配置项
     *
     * @param configKey 配置键
     * @return 系统配置实体
     */
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据配置分组查询配置列表
     *
     * @param configGroup 配置分组
     * @return 系统配置列表
     */
    List<SystemConfig> selectByConfigGroup(@Param("configGroup") String configGroup);

    /**
     * 根据配置类型查询配置列表
     *
     * @param configType 配置类型
     * @return 系统配置列表
     */
    List<SystemConfig> selectByConfigType(@Param("configType") SystemConfig.ConfigType configType);

    /**
     * 根据配置键直接获取配置值
     *
     * @param configKey 配置键
     * @return 配置值（如果不存在返回null）
     */
    @Select("SELECT config_value FROM system_config WHERE config_key = #{configKey} AND deleted = 0")
    String getConfigValue(@Param("configKey") String configKey);
}