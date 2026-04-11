package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.*;
import com.example.workcompetitionplatform.mapper.*;
import com.example.workcompetitionplatform.service.ITeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 团队服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements ITeamService {

    private final TeamMemberMapper teamMemberMapper;
    private final TeamInvitationMapper teamInvitationMapper;
    private final TeamApplicationMapper teamApplicationMapper;

    @Autowired
    private UserMapper userMapper;

    public TeamServiceImpl(TeamMemberMapper teamMemberMapper,
                           TeamInvitationMapper teamInvitationMapper,
                           TeamApplicationMapper teamApplicationMapper) {
        this.teamMemberMapper = teamMemberMapper;
        this.teamInvitationMapper = teamInvitationMapper;
        this.teamApplicationMapper = teamApplicationMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Team createTeam(String teamName, Long competitionTrackId, Long leaderId) {
        // 查询队长信息
        User leader = userMapper.selectById(leaderId);

        // 创建团队
        Team team = new Team();
        team.setTeamCode(generateTeamCode());
        team.setTeamName(teamName);
        team.setCompetitionTrackId(competitionTrackId);
        team.setLeaderId(leaderId);
        team.setStatus(Team.TeamStatus.FORMING);
        team.setCurrentMemberCount(1);

        // 保存团队
        save(team);

        // 创建队长成员记录（填充完整信息）
        TeamMember leaderMember = new TeamMember();
        leaderMember.setTeamId(team.getId());
        leaderMember.setUserId(leaderId);
        leaderMember.setMemberRole(TeamMember.MemberRole.LEADER);
        leaderMember.setStudentNo(leader != null ? leader.getStudentNo() : null);
        leaderMember.setRealName(leader != null ? leader.getRealName() : null);
        leaderMember.setMajor(leader != null ? leader.getMajor() : null);
        leaderMember.setCollege(leader != null ? leader.getCollege() : null);
        leaderMember.setJoinTime(java.time.LocalDateTime.now());
        teamMemberMapper.insert(leaderMember);

        return team;
    }

    @Override
    public Team getByTeamCode(String teamCode) {
        return baseMapper.selectByTeamCode(teamCode);
    }

    @Override
    public List<Team> listByLeaderId(Long leaderId) {
        return baseMapper.selectByLeaderId(leaderId);
    }

    @Override
    public List<Team> listByCompetitionTrackId(Long competitionTrackId) {
        return baseMapper.selectByCompetitionTrackId(competitionTrackId);
    }

    @Override
    public List<Team> listByStatus(Team.TeamStatus status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<Team> listUserTeams(Long userId) {
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getUserId, userId);
        List<TeamMember> members = teamMemberMapper.selectList(memberWrapper);

        if (members.isEmpty()) {
            return List.of();
        }

        List<Long> teamIds = members.stream()
                .map(TeamMember::getTeamId)
                .toList();

        LambdaQueryWrapper<Team> teamWrapper = new LambdaQueryWrapper<>();
        teamWrapper.in(Team::getId, teamIds);
        return list(teamWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeamInvitation inviteMember(Long teamId, Long inviterUserId, Long invitedUserId) {
        Team team = getById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查是否有邀请权限（队长）
        if (!team.getLeaderId().equals(inviterUserId)) {
            throw new RuntimeException("只有队长可以邀请成员");
        }

        // 检查团队是否已满员
        if (isTeamFull(teamId)) {
            throw new RuntimeException("团队已满员");
        }

        // 检查用户是否已在团队中
        if (isUserInTeam(teamId, invitedUserId)) {
            throw new RuntimeException("用户已在团队中");
        }

        // 创建邀请记录
        TeamInvitation invitation = new TeamInvitation();
        invitation.setTeamId(teamId);
        invitation.setInviterId(inviterUserId);
        invitation.setInviteeId(invitedUserId);
        invitation.setStatus(TeamInvitation.InvitationStatus.PENDING);
        teamInvitationMapper.insert(invitation);

        return invitation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeamApplication applyToJoin(Long teamId, Long applicantUserId, String applicationReason) {
        Team team = getById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查团队是否已满员
        if (isTeamFull(teamId)) {
            throw new RuntimeException("团队已满员");
        }

        // 检查用户是否已在团队中
        if (isUserInTeam(teamId, applicantUserId)) {
            throw new RuntimeException("用户已在团队中");
        }

        // 创建申请记录
        TeamApplication application = new TeamApplication();
        application.setTeamId(teamId);
        application.setApplicantId(applicantUserId);
        application.setMessage(applicationReason);
        application.setStatus(TeamApplication.ApplicationStatus.PENDING);
        teamApplicationMapper.insert(application);

        return application;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleInvitation(Long invitationId, Long invitedUserId, boolean accept) {
        TeamInvitation invitation = teamInvitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new RuntimeException("邀请不存在");
        }

        // 检查是否是被邀请人
        if (!invitation.getInviteeId().equals(invitedUserId)) {
            throw new RuntimeException("无权处理此邀请");
        }

        // 检查邀请状态
        if (invitation.getStatus() != TeamInvitation.InvitationStatus.PENDING) {
            throw new RuntimeException("邀请已被处理");
        }

        // 更新邀请状态
        invitation.setStatus(accept ? TeamInvitation.InvitationStatus.ACCEPTED : TeamInvitation.InvitationStatus.REJECTED);
        teamInvitationMapper.updateById(invitation);

        // 如果接受邀请，添加团队成员
        if (accept) {
            addTeamMember(invitation.getTeamId(), invitedUserId, TeamMember.MemberRole.MEMBER);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleApplication(Long applicationId, Long teamId, boolean accept, String responseReason) {
        TeamApplication application = teamApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }

        Team team = getById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查申请状态
        if (application.getStatus() != TeamApplication.ApplicationStatus.PENDING) {
            throw new RuntimeException("申请已被处理");
        }

        // 更新申请状态
        application.setStatus(accept ? TeamApplication.ApplicationStatus.ACCEPTED : TeamApplication.ApplicationStatus.REJECTED);
        application.setMessage(responseReason);
        teamApplicationMapper.updateById(application);

        // 如果接受申请，添加团队成员
        if (accept) {
            addTeamMember(teamId, application.getApplicantId(), TeamMember.MemberRole.MEMBER);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMember(Long teamId, Long memberId, Long operatorId) {
        Team team = getById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查是否有移除权限（队长）
        if (!team.getLeaderId().equals(operatorId)) {
            throw new RuntimeException("只有队长可以移除成员");
        }

        // 不能移除队长
        if (team.getLeaderId().equals(memberId)) {
            throw new RuntimeException("不能移除队长");
        }

        // 删除成员记录
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, memberId);
        int deleted = teamMemberMapper.delete(wrapper);

        if (deleted > 0) {
            // 更新团队成员数量
            updateTeamMemberCount(teamId);
        }

        return deleted > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(Long teamId, Long userId) {
        Team team = getById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 队长不能退出团队
        if (team.getLeaderId().equals(userId)) {
            throw new RuntimeException("队长不能退出团队");
        }

        // 删除成员记录
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId);
        int deleted = teamMemberMapper.delete(wrapper);

        if (deleted > 0) {
            // 更新团队成员数量
            updateTeamMemberCount(teamId);
        }

        return deleted > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmTeam(Long teamId) {
        Team team = getById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查团队状态
        if (team.getStatus() != Team.TeamStatus.FORMING) {
            throw new RuntimeException("团队不在组建状态");
        }

        // 检查团队成员数量
        if (getTeamMemberCount(teamId) < 1) {
            throw new RuntimeException("团队至少需要1名成员");
        }

        // 更新团队状态
        team.setStatus(Team.TeamStatus.CONFIRMED);
        return updateById(team);
    }

    @Override
    public List<TeamMember> listTeamMembers(Long teamId) {
        return teamMemberMapper.selectByTeamId(teamId);
    }

    @Override
    public List<TeamInvitation> listUserPendingInvitations(Long userId) {
        // 使用JOIN查询获取邀请列表（包含团队名和邀请人姓名）
        return teamInvitationMapper.selectInvitationsWithDetails(userId);
    }

    @Override
    public List<TeamApplication> listTeamPendingApplications(Long teamId) {
        return teamApplicationMapper.selectApplicationsWithDetails(teamId);
    }

    @Override
    public boolean isUserInTeam(Long teamId, Long userId) {
        return teamMemberMapper.selectByTeamIdAndUserId(teamId, userId) != null;
    }

    @Override
    public boolean isTeamFull(Long teamId) {
        Team team = getById(teamId);
        if (team == null) {
            return false;
        }
        return getTeamMemberCount(teamId) >= team.getMaxMemberCount();
    }

    @Override
    public int getTeamMemberCount(Long teamId) {
        return teamMemberMapper.countByTeamId(teamId);
    }

    /**
     * 添加团队成员
     */
    private void addTeamMember(Long teamId, Long userId, TeamMember.MemberRole role) {
        // 查询用户信息
        User user = userMapper.selectById(userId);

        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setMemberRole(role);
        member.setStudentNo(user != null ? user.getStudentNo() : null);
        member.setRealName(user != null ? user.getRealName() : null);
        member.setMajor(user != null ? user.getMajor() : null);
        member.setCollege(user != null ? user.getCollege() : null);
        member.setJoinTime(java.time.LocalDateTime.now());
        teamMemberMapper.insert(member);

        // 更新团队成员数量
        updateTeamMemberCount(teamId);
    }

    /**
     * 更新团队成员数量
     */
    private void updateTeamMemberCount(Long teamId) {
        Team team = getById(teamId);
        if (team != null) {
            team.setCurrentMemberCount(getTeamMemberCount(teamId));
            updateById(team);
        }
    }

    /**
     * 生成团队编码
     */
    private String generateTeamCode() {
        return "TEAM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dissolveTeam(Long teamId) {
        Team team = getById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 删除所有团队成员
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getTeamId, teamId);
        teamMemberMapper.delete(memberWrapper);

        // 删除所有团队邀请
        LambdaQueryWrapper<TeamInvitation> invitationWrapper = new LambdaQueryWrapper<>();
        invitationWrapper.eq(TeamInvitation::getTeamId, teamId);
        teamInvitationMapper.delete(invitationWrapper);

        // 删除所有团队申请
        LambdaQueryWrapper<TeamApplication> applicationWrapper = new LambdaQueryWrapper<>();
        applicationWrapper.eq(TeamApplication::getTeamId, teamId);
        teamApplicationMapper.delete(applicationWrapper);

        // 删除团队
        return removeById(teamId);
    }
}