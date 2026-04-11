package com.example.workcompetitionplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.entity.Team;
import com.example.workcompetitionplatform.entity.TeamApplication;
import com.example.workcompetitionplatform.entity.TeamInvitation;
import com.example.workcompetitionplatform.entity.TeamMember;
import com.example.workcompetitionplatform.exception.BusinessException;
import com.example.workcompetitionplatform.mapper.TeamApplicationMapper;
import com.example.workcompetitionplatform.mapper.TeamInvitationMapper;
import com.example.workcompetitionplatform.service.ITeamService;
import com.example.workcompetitionplatform.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团队管理控制器
 * 提供团队创建、查询、邀请、申请和成员管理的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "团队管理")
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private ITeamService teamService;

    @Autowired
    private TeamApplicationMapper teamApplicationMapper;

    @Autowired
    private TeamInvitationMapper teamInvitationMapper;

    /**
     * 查询所有团队
     * 管理员查询所有团队列表
     *
     * @return API响应（包含团队列表）
     */
    @Operation(summary = "查询所有团队")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('JUDGE')")
    public ApiResponse<List<Team>> listAllTeams() {
        List<Team> teams = teamService.list();
        return ApiResponse.success(teams);
    }

    /**
     * 创建团队
     * 用户创建新的参赛团队
     *
     * @param teamName 团队名称
     * @param competitionTrackId 赛道ID
     * @return API响应（包含创建的团队）
     */
    @Operation(summary ="创建团队")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ApiResponse<Team> createTeam(
            @Parameter(description ="团队名称") @RequestParam String teamName,
            @Parameter(description ="赛道ID") @RequestParam Long competitionTrackId) {

        try {
            // 获取当前用户ID作为队长
            Long leaderId = UserContext.getCurrentUserId();

            // 创建团队
            Team team = teamService.createTeam(teamName, competitionTrackId, leaderId);

            log.info("团队创建成功：{}", team.getTeamCode());

            return ApiResponse.success("团队创建成功", team);
        } catch (BusinessException e) {
            log.error("团队创建失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询团队详情
     * 根据团队ID查询团队详细信息
     *
     * @param id 团队ID
     * @return API响应（包含团队详情）
     */
    @Operation(summary ="查询团队详情")
    @GetMapping("/{id}")
    public ApiResponse<Team> getTeamById(@Parameter(description ="团队ID") @PathVariable Long id) {
        Team team = teamService.getById(id);

        if (team == null) {
            return ApiResponse.notFound("团队不存在");
        }

        return ApiResponse.success(team);
    }

    /**
     * 查询我的团队
     * 查询当前用户所在的团队列表
     *
     * @return API响应（包含团队列表）
     */
    @Operation(summary ="查询我的团队")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Team>> listMyTeams() {
        Long userId = UserContext.getCurrentUserId();
        List<Team> teams = teamService.listUserTeams(userId);
        return ApiResponse.success(teams);
    }

    /**
     * 查询团队成员
     * 查询指定团队的成员列表
     *
     * @param id 团队ID
     * @return API响应（包含团队成员列表）
     */
    @Operation(summary ="查询团队成员")
    @GetMapping("/{id}/members")
    public ApiResponse<List<TeamMember>> listTeamMembers(@Parameter(description ="团队ID") @PathVariable Long id) {
        List<TeamMember> members = teamService.listTeamMembers(id);
        return ApiResponse.success(members);
    }

    /**
     * 邀请成员
     * 队长邀请用户加入团队
     *
     * @param id 团队ID
     * @param invitedUserId 被邀请用户ID
     * @return API响应（包含团队邀请）
     */
    @Operation(summary ="邀请成员")
    @PostMapping("/{id}/invite")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TeamInvitation> inviteMember(
            @Parameter(description ="团队ID") @PathVariable Long id,
            @Parameter(description ="被邀请用户ID") @RequestParam Long invitedUserId) {

        try {
            // 获取当前用户ID作为邀请人
            Long inviterUserId = UserContext.getCurrentUserId();

            // 验证当前用户是否是队长
            Team team = teamService.getById(id);
            if (team == null) {
                return ApiResponse.notFound("团队不存在");
            }

            if (!team.getLeaderId().equals(inviterUserId)) {
                return ApiResponse.forbidden("只有队长可以邀请成员");
            }

            // 执行邀请
            TeamInvitation invitation = teamService.inviteMember(id, inviterUserId, invitedUserId);

            log.info("成员邀请成功：团队 {} -> 用户 {}", id, invitedUserId);

            return ApiResponse.success("邀请已发送", invitation);
        } catch (BusinessException e) {
            log.error("成员邀请失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 申请加入
     * 用户申请加入指定团队
     *
     * @param id 团队ID
     * @param applicationReason 申请理由
     * @return API响应（包含团队申请）
     */
    @Operation(summary ="申请加入")
    @PostMapping("/{id}/apply")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TeamApplication> applyToJoin(
            @Parameter(description ="团队ID") @PathVariable Long id,
            @Parameter(description ="申请理由") @RequestParam(required = false) String applicationReason) {

        try {
            // 获取当前用户ID作为申请人
            Long applicantUserId = UserContext.getCurrentUserId();

            // 执行申请
            TeamApplication application = teamService.applyToJoin(id, applicantUserId, applicationReason);

            log.info("申请加入成功：用户 {} -> 团队 {}", applicantUserId, id);

            return ApiResponse.success("申请已提交", application);
        } catch (BusinessException e) {
            log.error("申请加入失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 接受邀请
     * 用户接受团队的邀请
     *
     * @param id 邀请ID
     * @return API响应
     */
    @Operation(summary ="接受邀请")
    @PostMapping("/invitations/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> acceptInvitation(@Parameter(description ="邀请ID") @PathVariable Long id) {
        try {
            // 获取当前用户ID
            Long userId = UserContext.getCurrentUserId();

            // 执行接受邀请
            boolean success = teamService.handleInvitation(id, userId, true);

            if (!success) {
                return ApiResponse.error("接受邀请失败");
            }

            log.info("用户 {} 接受了邀请 {}", userId, id);

            return ApiResponse.success("已接受邀请");
        } catch (BusinessException e) {
            log.error("接受邀请失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 拒绝邀请
     * 用户拒绝团队的邀请
     *
     * @param id 邀请ID
     * @return API响应
     */
    @Operation(summary ="拒绝邀请")
    @PostMapping("/invitations/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> rejectInvitation(@Parameter(description ="邀请ID") @PathVariable Long id) {
        try {
            // 获取当前用户ID
            Long userId = UserContext.getCurrentUserId();

            // 执行拒绝邀请
            boolean success = teamService.handleInvitation(id, userId, false);

            if (!success) {
                return ApiResponse.error("拒绝邀请失败");
            }

            log.info("用户 {} 拒绝了邀请 {}", userId, id);

            return ApiResponse.success("已拒绝邀请");
        } catch (BusinessException e) {
            log.error("拒绝邀请失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 接受申请
     * 队长接受用户的加入申请
     *
     * @param id 申请ID
     * @return API响应
     */
    @Operation(summary ="接受申请")
    @PostMapping("/applications/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> acceptApplication(@Parameter(description ="申请ID") @PathVariable Long id) {
        try {
            // 获取当前用户ID
            Long userId = UserContext.getCurrentUserId();

            // 查询申请信息
            TeamApplication application = teamApplicationMapper.selectById(id);
            if (application == null) {
                return ApiResponse.notFound("申请不存在");
            }

            // 验证当前用户是否是队长
            Team team = teamService.getById(application.getTeamId());
            if (!team.getLeaderId().equals(userId)) {
                return ApiResponse.forbidden("只有队长可以接受申请");
            }

            // 执行接受申请
            boolean success = teamService.handleApplication(id, application.getTeamId(), true, null);

            if (!success) {
                return ApiResponse.error("接受申请失败");
            }

            log.info("队长 {} 接受了申请 {}", userId, id);

            return ApiResponse.success("已接受申请");
        } catch (BusinessException e) {
            log.error("接受申请失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 拒绝申请
     * 队长拒绝用户的加入申请
     *
     * @param id 申请ID
     * @param responseReason 拒绝理由
     * @return API响应
     */
    @Operation(summary ="拒绝申请")
    @PostMapping("/applications/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> rejectApplication(
            @Parameter(description ="申请ID") @PathVariable Long id,
            @Parameter(description ="拒绝理由") @RequestParam(required = false) String responseReason) {

        try {
            // 获取当前用户ID
            Long userId = UserContext.getCurrentUserId();

            // 查询申请信息
            TeamApplication application = teamApplicationMapper.selectById(id);
            if (application == null) {
                return ApiResponse.notFound("申请不存在");
            }

            // 验证当前用户是否是队长
            Team team = teamService.getById(application.getTeamId());
            if (!team.getLeaderId().equals(userId)) {
                return ApiResponse.forbidden("只有队长可以拒绝申请");
            }

            // 执行拒绝申请
            boolean success = teamService.handleApplication(id, application.getTeamId(), false, responseReason);

            if (!success) {
                return ApiResponse.error("拒绝申请失败");
            }

            log.info("队长 {} 拒绝了申请 {}", userId, id);

            return ApiResponse.success("已拒绝申请");
        } catch (BusinessException e) {
            log.error("拒绝申请失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 移除成员
     * 队长移除团队成员
     *
     * @param id 团队ID
     * @param memberId 成员ID
     * @return API响应
     */
    @Operation(summary ="移除成员")
    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> removeMember(
            @Parameter(description ="团队ID") @PathVariable Long id,
            @Parameter(description ="成员ID") @PathVariable Long memberId) {

        try {
            // 获取当前用户ID
            Long userId = UserContext.getCurrentUserId();

            // 执行移除成员
            boolean success = teamService.removeMember(id, memberId, userId);

            if (!success) {
                return ApiResponse.error("移除成员失败");
            }

            log.info("队长 {} 移除了成员 {} from 团队 {}", userId, memberId, id);

            return ApiResponse.success("成员已移除");
        } catch (BusinessException e) {
            log.error("移除成员失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 退出团队
     * 成员退出所在的团队
     *
     * @param id 团队ID
     * @return API响应
     */
    @Operation(summary ="退出团队")
    @PostMapping("/{id}/quit")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> quitTeam(@Parameter(description ="团队ID") @PathVariable Long id) {
        try {
            // 获取当前用户ID
            Long userId = UserContext.getCurrentUserId();

            // 执行退出团队
            boolean success = teamService.quitTeam(id, userId);

            if (!success) {
                return ApiResponse.error("退出团队失败");
            }

            log.info("用户 {} 退出了团队 {}", userId, id);

            return ApiResponse.success("已退出团队");
        } catch (BusinessException e) {
            log.error("退出团队失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 确认团队
     * 队长确认团队组建完成
     *
     * @param id 团队ID
     * @return API响应
     */
    @Operation(summary ="确认团队")
    @PostMapping("/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> confirmTeam(@Parameter(description ="团队ID") @PathVariable Long id) {
        try {
            // 获取当前用户ID
            Long userId = UserContext.getCurrentUserId();

            // 验证是否是队长
            Team team = teamService.getById(id);
            if (team == null) {
                return ApiResponse.notFound("团队不存在");
            }

            if (!team.getLeaderId().equals(userId)) {
                return ApiResponse.forbidden("只有队长可以确认团队");
            }

            // 执行确认团队
            boolean success = teamService.confirmTeam(id);

            if (!success) {
                return ApiResponse.error("确认团队失败");
            }

            log.info("队长 {} 确认了团队 {}", userId, id);

            return ApiResponse.success("团队已确认");
        } catch (BusinessException e) {
            log.error("确认团队失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 解散团队
     * 队长解散团队
     *
     * @param id 团队ID
     * @return API响应
     */
    @Operation(summary ="解散团队")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> dissolveTeam(@Parameter(description ="团队ID") @PathVariable Long id) {
        try {
            // 获取当前用户ID
            Long userId = UserContext.getCurrentUserId();

            // 验证是否是队长
            Team team = teamService.getById(id);
            if (team == null) {
                return ApiResponse.notFound("团队不存在");
            }

            if (!team.getLeaderId().equals(userId)) {
                return ApiResponse.forbidden("只有队长可以解散团队");
            }

            // 执行解散团队
            boolean success = teamService.dissolveTeam(id);

            if (!success) {
                return ApiResponse.error("解散团队失败");
            }

            log.info("队长 {} 解散了团队 {}", userId, id);

            return ApiResponse.success("团队已解散");
        } catch (BusinessException e) {
            log.error("解散团队失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询我的申请列表
     *
     * @return API响应（包含申请列表）
     */
    @Operation(summary ="查询我的申请列表")
    @GetMapping("/applications/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TeamApplication>> listMyApplications() {
        Long userId = UserContext.getCurrentUserId();
        List<TeamApplication> applications = teamApplicationMapper.selectMyApplicationsWithDetails(userId);
        return ApiResponse.success(applications);
    }

    /**
     * 取消申请
     * 用户取消自己的团队申请
     *
     * @param id 申请ID
     * @return API响应
     */
    @Operation(summary ="取消申请")
    @DeleteMapping("/applications/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> cancelApplication(@Parameter(description ="申请ID") @PathVariable Long id) {
        try {
            // 获取当前用户ID
            Long userId = UserContext.getCurrentUserId();

            // 查询申请信息
            TeamApplication application = teamApplicationMapper.selectById(id);
            if (application == null) {
                return ApiResponse.notFound("申请不存在");
            }

            // 验证是否是本人的申请
            if (!application.getApplicantId().equals(userId)) {
                return ApiResponse.forbidden("只能取消自己的申请");
            }

            // 验证申请状态是否可以取消
            if (application.getStatus() != TeamApplication.ApplicationStatus.PENDING) {
                return ApiResponse.error("只能取消待处理的申请");
            }

            // 删除申请
            teamApplicationMapper.deleteById(id);

            log.info("用户 {} 取消了申请 {}", userId, id);

            return ApiResponse.success("申请已取消");
        } catch (Exception e) {
            log.error("取消申请失败", e);
            return ApiResponse.error("取消申请失败");
        }
    }

    /**
     * 搜索团队
     * 根据团队名称搜索团队
     *
     * @param keyword 搜索关键词（团队名称）
     * @return API响应（包含团队列表）
     */
    @Operation(summary ="搜索团队")
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Team>> searchTeams(
            @Parameter(description ="搜索关键词") @RequestParam String keyword) {

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        // 添加搜索关键词条件
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like("team_name", keyword);
        }

        // 只查询组建中的团队
        queryWrapper.eq("status", "FORMING");

        // 按创建时间降序排序
        queryWrapper.orderByDesc("create_time");

        List<Team> teams = teamService.list(queryWrapper);

        return ApiResponse.success(teams);
    }

    /**
     * 查询我的待处理邀请
     *
     * @return API响应（包含邀请列表）
     */
    @Operation(summary ="查询我的待处理邀请")
    @GetMapping("/invitations/pending")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TeamInvitation>> listMyPendingInvitations() {
        Long userId = UserContext.getCurrentUserId();
        List<TeamInvitation> invitations = teamService.listUserPendingInvitations(userId);
        return ApiResponse.success(invitations);
    }

    /**
     * 查询团队的待处理申请
     *
     * @param id 团队ID
     * @return API响应（包含申请列表）
     */
    @Operation(summary ="查询团队的待处理申请")
    @GetMapping("/{id}/applications/pending")
    public ApiResponse<List<TeamApplication>> listTeamPendingApplications(@Parameter(description ="团队ID") @PathVariable Long id) {
        List<TeamApplication> applications = teamService.listTeamPendingApplications(id);
        return ApiResponse.success(applications);
    }
}