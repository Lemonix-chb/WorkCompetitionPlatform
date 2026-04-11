package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.entity.Team;
import com.example.workcompetitionplatform.entity.TeamMember;
import com.example.workcompetitionplatform.entity.TeamInvitation;
import com.example.workcompetitionplatform.entity.TeamApplication;

import java.util.List;

/**
 * 团队服务接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public interface ITeamService extends IService<Team> {

    /**
     * 创建团队
     *
     * @param teamName 团队名称
     * @param competitionTrackId 赛道ID
     * @param leaderId 队长ID
     * @return 团队实体
     */
    Team createTeam(String teamName, Long competitionTrackId, Long leaderId);

    /**
     * 根据团队编码查询团队
     *
     * @param teamCode 团队编码
     * @return 团队实体
     */
    Team getByTeamCode(String teamCode);

    /**
     * 根据队长ID查询团队列表
     *
     * @param leaderId 队长ID
     * @return 团队列表
     */
    List<Team> listByLeaderId(Long leaderId);

    /**
     * 根据赛道ID查询团队列表
     *
     * @param competitionTrackId 赛道ID
     * @return 团队列表
     */
    List<Team> listByCompetitionTrackId(Long competitionTrackId);

    /**
     * 根据团队状态查询团队列表
     *
     * @param status 团队状态
     * @return 团队列表
     */
    List<Team> listByStatus(Team.TeamStatus status);

    /**
     * 查询用户所在的团队列表
     *
     * @param userId 用户ID
     * @return 团队列表
     */
    List<Team> listUserTeams(Long userId);

    /**
     * 邀请用户加入团队
     *
     * @param teamId 团队ID
     * @param inviterUserId 邀请人ID（队长）
     * @param invitedUserId 被邀请人ID
     * @return 团队邀请实体
     */
    TeamInvitation inviteMember(Long teamId, Long inviterUserId, Long invitedUserId);

    /**
     * 用户申请加入团队
     *
     * @param teamId 团队ID
     * @param applicantUserId 申请人ID
     * @param applicationReason 申请理由
     * @return 团队申请实体
     */
    TeamApplication applyToJoin(Long teamId, Long applicantUserId, String applicationReason);

    /**
     * 处理团队邀请（接受或拒绝）
     *
     * @param invitationId 邀请ID
     * @param invitedUserId 被邀请人ID
     * @param accept 是否接受
     * @return 是否成功
     */
    boolean handleInvitation(Long invitationId, Long invitedUserId, boolean accept);

    /**
     * 处理团队申请（接受或拒绝）
     *
     * @param applicationId 申请ID
     * @param teamId 团队ID
     * @param accept 是否接受
     * @param responseReason 回复理由
     * @return 是否成功
     */
    boolean handleApplication(Long applicationId, Long teamId, boolean accept, String responseReason);

    /**
     * 移除团队成员
     *
     * @param teamId 团队ID
     * @param memberId 成员ID
     * @param operatorId 操作人ID（队长）
     * @return 是否成功
     */
    boolean removeMember(Long teamId, Long memberId, Long operatorId);

    /**
     * 成员退出团队
     *
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean quitTeam(Long teamId, Long userId);

    /**
     * 确认团队（锁定团队成员）
     *
     * @param teamId 团队ID
     * @return 是否成功
     */
    boolean confirmTeam(Long teamId);

    /**
     * 查询团队成员列表
     *
     * @param teamId 团队ID
     * @return 团队成员列表
     */
    List<TeamMember> listTeamMembers(Long teamId);

    /**
     * 查询用户的待处理邀请
     *
     * @param userId 用户ID
     * @return 团队邀请列表
     */
    List<TeamInvitation> listUserPendingInvitations(Long userId);

    /**
     * 查询团队的待处理申请
     *
     * @param teamId 团队ID
     * @return 团队申请列表
     */
    List<TeamApplication> listTeamPendingApplications(Long teamId);

    /**
     * 检查用户是否在团队中
     *
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return 是否在团队中
     */
    boolean isUserInTeam(Long teamId, Long userId);

    /**
     * 检查团队是否已满员
     *
     * @param teamId 团队ID
     * @return 是否已满员
     */
    boolean isTeamFull(Long teamId);

    /**
     * 获取团队当前成员数量
     *
     * @param teamId 团队ID
     * @return 成员数量
     */
    int getTeamMemberCount(Long teamId);

    /**
     * 解散团队
     *
     * @param teamId 团队ID
     * @return 是否成功
     */
    boolean dissolveTeam(Long teamId);
}