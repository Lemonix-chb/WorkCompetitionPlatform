package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.annotation.RateLimit;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.entity.Competition;
import com.example.workcompetitionplatform.entity.Registration;
import com.example.workcompetitionplatform.entity.Team;
import com.example.workcompetitionplatform.mapper.CompetitionMapper;
import com.example.workcompetitionplatform.mapper.RegistrationMapper;
import com.example.workcompetitionplatform.mapper.TeamMapper;
import com.example.workcompetitionplatform.service.ITeamService;
import com.example.workcompetitionplatform.util.DateTimeConstants;
import com.example.workcompetitionplatform.util.UserContext;
import com.example.workcompetitionplatform.util.WorkCodeGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报名管理控制器
 * 提报名记录查询和管理的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "报名管理")
@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private ITeamService teamService;

    /**
     * 创建报名记录
     * 团队报名参加赛事（包含完整的权限验证）
     * 速率限制：每个用户每分钟最多5次报名请求
     *
     * @param competitionId 赛事ID
     * @param trackId 赛道ID
     * @param teamId 团队ID
     * @return API响应（包含创建的报名记录）
     */
    @Operation(summary = "创建报名记录")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @RateLimit(value = 5, timeout = 60, message = "报名请求过于频繁，请等待1分钟后再试")
    @Transactional
    public ApiResponse<Registration> createRegistration(
            @Parameter(description = "赛事ID") @RequestParam Long competitionId,
            @Parameter(description = "赛道ID") @RequestParam Long trackId,
            @Parameter(description = "团队ID") @RequestParam Long teamId) {

        try {
            // 验证团队是否存在
            Team team = teamMapper.selectById(teamId);
            if (team == null) {
                return ApiResponse.notFound("团队不存在");
            }

            // 验证当前用户是否是团队成员
            Long userId = UserContext.getCurrentUserId();
            if (!teamService.isUserInTeam(teamId, userId)) {
                log.warn("用户 {} 尝试报名团队 {} 但不是团队成员", userId, teamId);
                return ApiResponse.forbidden("只有团队成员可以报名");
            }

            // 检查是否已经报名该赛事
            Registration existing = registrationMapper.selectByCompetitionIdAndUserId(competitionId, userId);
            if (existing != null) {
                log.warn("用户 {} 重复报名赛事 {}", userId, competitionId);
                return ApiResponse.error("您已经报名了该赛事，不能重复报名");
            }

            // 检查团队状态（必须已确认）
            if (team.getStatus() != Team.TeamStatus.CONFIRMED &&
                team.getStatus() != Team.TeamStatus.REGISTERED) {
                return ApiResponse.error("团队状态不符合报名要求，请先确认团队");
            }

            // 检查是否在报名期内（使用预加载的competition避免重复查询）
            Competition competition = competitionMapper.selectById(competitionId);
            LocalDateTime now = DateTimeConstants.now();
            if (!now.isAfter(competition.getRegistrationStart()) ||
                !now.isBefore(competition.getRegistrationEnd())) {
                String endTime = competition.getRegistrationEnd()
                    .format(DateTimeConstants.STANDARD_FORMAT);
                return ApiResponse.error("报名已截止，截止时间：" + endTime);
            }

            // 创建报名记录
            Registration registration = new Registration();
            registration.setTeamId(teamId);
            registration.setCompetitionId(competitionId);
            registration.setTrackId(trackId);
            registration.setRegistrationCode(WorkCodeGenerator.generateRegistrationCode());
            registration.setSubmitterId(userId);
            registration.setRegistrationTime(LocalDateTime.now());
            registration.setStatus(Registration.RegistrationStatus.APPROVED);

            registrationMapper.insert(registration);

            // 更新团队状态为已报名
            team.setStatus(Team.TeamStatus.REGISTERED);
            teamMapper.updateById(team);

            log.info("报名成功：用户 {} 团队 {} 报名赛事 {} 赛道 {}，报名编号 {}",
                    userId, teamId, competitionId, trackId, registration.getRegistrationCode());

            return ApiResponse.success("报名成功", registration);
        } catch (Exception e) {
            log.error("报名失败", e);
            return ApiResponse.error("报名失败：" + e.getMessage());
        }
    }

    /**
     * 查询我的报名记录
     * 查询当前用户所有的报名记录
     *
     * @return API响应（包含报名记录列表）
     */
    @Operation(summary = "查询我的报名记录")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Registration>> listMyRegistrations() {
        Long userId = UserContext.getCurrentUserId();
        List<Registration> registrations = registrationMapper.selectByUserId(userId);
        return ApiResponse.success(registrations);
    }
}