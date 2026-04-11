package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.TeamRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队报名Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface TeamRegistrationMapper extends BaseMapper<TeamRegistration> {

    /**
     * 根据赛事ID查询所有团队报名记录
     *
     * @param competitionId 赛事ID
     * @return 团队报名记录列表
     */
    List<TeamRegistration> selectByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 根据团队ID查询团队报名记录
     *
     * @param teamId 团队ID
     * @return 团队报名记录列表
     */
    List<TeamRegistration> selectByTeamId(@Param("teamId") Long teamId);

    /**
     * 根据赛道ID查询团队报名记录
     *
     * @param competitionTrackId 赛道ID
     * @return 团队报名记录列表
     */
    List<TeamRegistration> selectByCompetitionTrackId(@Param("competitionTrackId") Long competitionTrackId);

    /**
     * 根据报名状态查询团队报名记录
     *
     * @param status 报名状态
     * @return 团队报名记录列表
     */
    List<TeamRegistration> selectByStatus(@Param("status") TeamRegistration.TeamRegistrationStatus status);

    /**
     * 根据赛事ID和团队ID查询团队报名记录
     *
     * @param competitionId 赛事ID
     * @param teamId 团队ID
     * @return 团队报名记录实体
     */
    TeamRegistration selectByCompetitionIdAndTeamId(@Param("competitionId") Long competitionId, @Param("teamId") Long teamId);
}