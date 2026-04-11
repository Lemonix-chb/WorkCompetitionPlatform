package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.TeamInvitation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队邀请Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface TeamInvitationMapper extends BaseMapper<TeamInvitation> {

    /**
     * 根据团队ID查询所有邀请
     *
     * @param teamId 团队ID
     * @return 团队邀请列表
     */
    List<TeamInvitation> selectByTeamId(@Param("teamId") Long teamId);

    /**
     * 根据被邀请用户ID查询所有邀请
     *
     * @param invitedUserId 被邀请用户ID
     * @return 团队邀请列表
     */
    List<TeamInvitation> selectByInvitedUserId(@Param("invitedUserId") Long invitedUserId);

    /**
     * 根据邀请状态查询邀请列表
     *
     * @param status 邀请状态
     * @return 团队邀请列表
     */
    List<TeamInvitation> selectByStatus(@Param("status") TeamInvitation.InvitationStatus status);

    /**
     * 根据团队ID和被邀请用户ID查询邀请记录
     *
     * @param teamId 团队ID
     * @param invitedUserId 被邀请用户ID
     * @return 团队邀请实体
     */
    TeamInvitation selectByTeamIdAndInvitedUserId(@Param("teamId") Long teamId, @Param("invitedUserId") Long invitedUserId);

    /**
     * 根据被邀请用户ID查询邀请列表（包含团队名和邀请人姓名）
     *
     * @param invitedUserId 被邀请用户ID
     * @return 团队邀请列表（包含团队名和邀请人姓名）
     */
    List<TeamInvitation> selectInvitationsWithDetails(@Param("invitedUserId") Long invitedUserId);
}