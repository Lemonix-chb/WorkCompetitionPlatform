package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报名记录Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    /**
     * 根据赛事ID查询所有报名记录
     *
     * @param competitionId 赛事ID
     * @return 报名记录列表
     */
    List<Registration> selectByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 根据用户ID查询所有报名记录
     *
     * @param userId 用户ID
     * @return 报名记录列表
     */
    List<Registration> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据赛道ID查询报名记录
     *
     * @param competitionTrackId 赛道ID
     * @return 报名记录列表
     */
    List<Registration> selectByCompetitionTrackId(@Param("competitionTrackId") Long competitionTrackId);

    /**
     * 根据报名状态查询报名记录
     *
     * @param status 报名状态
     * @return 报名记录列表
     */
    List<Registration> selectByStatus(@Param("status") Registration.RegistrationStatus status);

    /**
     * 根据赛事ID和用户ID查询报名记录
     *
     * @param competitionId 赛事ID
     * @param userId 用户ID
     * @return 报名记录实体
     */
    Registration selectByCompetitionIdAndUserId(@Param("competitionId") Long competitionId, @Param("userId") Long userId);
}