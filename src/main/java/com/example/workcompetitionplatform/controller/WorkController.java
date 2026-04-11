package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.entity.CompetitionTrack;
import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.exception.BusinessException;
import com.example.workcompetitionplatform.mapper.CompetitionTrackMapper;
import com.example.workcompetitionplatform.service.IWorkService;
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
 * 作品管理控制器
 * 提供作品创建、查询、更新和提交的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "作品管理")
@RestController
@RequestMapping("/api/works")
public class WorkController {

    @Autowired
    private IWorkService workService;

    @Autowired
    private CompetitionTrackMapper competitionTrackMapper;

    /**
     * 创建作品
     * 团队创建参赛作品
     *
     * @param workName 作品名称
     * @param teamId 团队ID
     * @param competitionId 赛事ID
     * @param trackId 赛道ID
     * @return API响应（包含创建的作品）
     */
    @Operation(summary = "创建作品")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ApiResponse<Work> createWork(
            @Parameter(description = "作品名称") @RequestParam String workName,
            @Parameter(description = "团队ID") @RequestParam Long teamId,
            @Parameter(description = "赛事ID") @RequestParam Long competitionId,
            @Parameter(description = "赛道ID") @RequestParam Long trackId) {

        try {
            // 获取赛道信息以确定作品类型
            CompetitionTrack track = competitionTrackMapper.selectById(trackId);
            if (track == null) {
                return ApiResponse.notFound("赛道不存在");
            }

            // 根据赛道类型确定作品类型
            Work.WorkType workType;
            try {
                workType = Work.WorkType.valueOf(track.getTrackType().name());
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("不支持的赛道类型");
            }

            // 创建作品
            Work work = workService.createWork(workName, teamId, competitionId, trackId, workType);

            log.info("作品创建成功：{}", work.getWorkCode());

            return ApiResponse.success("作品创建成功", work);
        } catch (BusinessException e) {
            log.error("作品创建失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询作品详情
     * 根据作品ID查询作品详细信息
     *
     * @param id 作品ID
     * @return API响应（包含作品详情）
     */
    @Operation(summary = "查询作品详情")
    @GetMapping("/{id}")
    public ApiResponse<Work> getWorkById(@Parameter(description = "作品ID") @PathVariable Long id) {
        Work work = workService.getWorkDetail(id);

        if (work == null) {
            return ApiResponse.notFound("作品不存在");
        }

        return ApiResponse.success(work);
    }

    /**
     * 查询我的作品
     * 查询当前用户所在团队的作品列表
     *
     * @return API响应（包含作品列表）
     */
    @Operation(summary = "查询我的作品")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Work>> listMyWorks() {
        Long userId = UserContext.getCurrentUserId();
        List<Work> works = workService.listUserWorks(userId);
        return ApiResponse.success(works);
    }

    /**
     * 查询团队作品
     * 查询指定团队的作品列表
     *
     * @param teamId 团队ID
     * @return API响应（包含作品列表）
     */
    @Operation(summary = "查询团队作品")
    @GetMapping("/team/{teamId}")
    public ApiResponse<List<Work>> listTeamWorks(@Parameter(description = "团队ID") @PathVariable Long teamId) {
        List<Work> works = workService.listByTeamId(teamId);
        return ApiResponse.success(works);
    }

    /**
     * 查询赛道作品
     * 查询指定赛道下的作品列表
     *
     * @param trackId 赛道ID
     * @return API响应（包含作品列表）
     */
    @Operation(summary = "查询赛道作品")
    @GetMapping("/track/{trackId}")
    public ApiResponse<List<Work>> listTrackWorks(@Parameter(description = "赛道ID") @PathVariable Long trackId) {
        List<Work> works = workService.listByTrackId(trackId);
        return ApiResponse.success(works);
    }

    /**
     * 查询赛事作品
     * 查询指定赛事下的作品列表
     *
     * @param competitionId 赛事ID
     * @return API响应（包含作品列表）
     */
    @Operation(summary = "查询赛事作品")
    @GetMapping("/competition/{competitionId}")
    public ApiResponse<List<Work>> listCompetitionWorks(@Parameter(description = "赛事ID") @PathVariable Long competitionId) {
        List<Work> works = workService.listByCompetitionId(competitionId);
        return ApiResponse.success(works);
    }

    /**
     * 查询所有作品
     * 查询系统中所有的作品（管理员）
     *
     * @return API响应（包含作品列表）
     */
    @Operation(summary = "查询所有作品")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('JUDGE')")
    public ApiResponse<List<Work>> listAllWorks() {
        List<Work> works = workService.list();
        return ApiResponse.success(works);
    }

    /**
     * 更新作品信息
     * 更新作品的基本信息
     *
     * @param id 作品ID
     * @param workName 作品名称
     * @param description 作品简介
     * @param innovationPoints 创新点说明
     * @param keyFeatures 关键功能特性
     * @param techStack 技术栈
     * @param divisionOfLabor 团队分工说明
     * @param targetUsers 目标用户/应用场景
     * @return API响应
     */
    @Operation(summary = "更新作品信息")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> updateWorkInfo(
            @Parameter(description = "作品ID") @PathVariable Long id,
            @Parameter(description = "作品名称") @RequestParam String workName,
            @Parameter(description = "作品简介") @RequestParam(required = false) String description,
            @Parameter(description = "创新点说明") @RequestParam(required = false) String innovationPoints,
            @Parameter(description = "关键功能特性") @RequestParam(required = false) String keyFeatures,
            @Parameter(description = "技术栈") @RequestParam(required = false) String techStack,
            @Parameter(description = "团队分工说明") @RequestParam(required = false) String divisionOfLabor,
            @Parameter(description = "目标用户/应用场景") @RequestParam(required = false) String targetUsers) {

        try {
            boolean success = workService.updateWorkInfo(id, workName, description, innovationPoints,
                    keyFeatures, techStack, divisionOfLabor, targetUsers);

            if (!success) {
                return ApiResponse.error("更新作品信息失败");
            }

            log.info("作品信息更新成功：作品ID {}", id);

            return ApiResponse.success("作品信息更新成功");
        } catch (BusinessException e) {
            log.error("更新作品信息失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 提交作品
     * 将作品提交给评审
     *
     * @param id 作品ID
     * @return API响应
     */
    @Operation(summary = "提交作品")
    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> submitWork(@Parameter(description = "作品ID") @PathVariable Long id) {
        try {
            boolean success = workService.submitWork(id);

            if (!success) {
                return ApiResponse.error("提交作品失败");
            }

            log.info("作品提交成功：作品ID {}", id);

            return ApiResponse.success("作品提交成功");
        } catch (BusinessException e) {
            log.error("提交作品失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 标记作品完成
     * 学生将作品状态标记为已完成（准备提交）
     *
     * @param id 作品ID
     * @return API响应
     */
    @Operation(summary = "标记作品完成")
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> markWorkComplete(@Parameter(description = "作品ID") @PathVariable Long id) {
        try {
            // 验证用户权限（必须是团队成员）
            Long userId = UserContext.getCurrentUserId();
            Work work = workService.getById(id);

            if (work == null) {
                return ApiResponse.notFound("作品不存在");
            }

            // 标记作品完成
            boolean success = workService.markWorkComplete(id, userId);

            if (!success) {
                return ApiResponse.error("标记完成失败");
            }

            log.info("作品标记完成：作品ID {}, 用户ID {}", id, userId);

            return ApiResponse.success("作品已标记为完成");
        } catch (BusinessException e) {
            log.error("标记作品完成失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}