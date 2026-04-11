package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

    /**
     * 根据团队编码查询团队
     *
     * @param teamCode 团队编码
     * @return 团队实体
     */
    Team selectByTeamCode(@Param("teamCode") String teamCode);

    /**
     * 根据队长ID查询团队列表
     *
     * @param leaderId 队长用户ID
     * @return 团队列表
     */
    List<Team> selectByLeaderId(@Param("leaderId") Long leaderId);

    /**
     * 根据赛道ID查询团队列表
     *
     * @param competitionTrackId 赛道ID
     * @return 团队列表
     */
    List<Team> selectByCompetitionTrackId(@Param("competitionTrackId") Long competitionTrackId);

    /**
     * 根据团队状态查询团队列表
     *
     * @param status 团队状态
     * @return 团队列表
     */
    List<Team> selectByStatus(@Param("status") Team.TeamStatus status);
}