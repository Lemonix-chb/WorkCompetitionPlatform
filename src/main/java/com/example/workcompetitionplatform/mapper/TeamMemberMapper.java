package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队成员Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {

    /**
     * 根据团队ID查询所有成员
     *
     * @param teamId 团队ID
     * @return 团队成员列表
     */
    List<TeamMember> selectByTeamId(@Param("teamId") Long teamId);

    /**
     * 根据用户ID查询用户所在的团队成员记录
     *
     * @param userId 用户ID
     * @return 团队成员列表
     */
    List<TeamMember> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据团队ID和用户ID查询团队成员记录
     *
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return 团队成员实体
     */
    TeamMember selectByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);

    /**
     * 根据团队ID统计成员数量
     *
     * @param teamId 团队ID
     * @return 成员数量
     */
    int countByTeamId(@Param("teamId") Long teamId);

    /**
     * 删除团队的所有成员
     *
     * @param teamId 团队ID
     * @return 删除的记录数
     */
    int deleteByTeamId(@Param("teamId") Long teamId);
}