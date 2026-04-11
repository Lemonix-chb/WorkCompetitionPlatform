package com.example.workcompetitionplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.dto.PageResponse;
import com.example.workcompetitionplatform.entity.Competition;
import com.example.workcompetitionplatform.entity.CompetitionTrack;
import com.example.workcompetitionplatform.exception.BusinessException;
import com.example.workcompetitionplatform.service.ICompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 赛事管理控制器
 * 提供赛事查询、创建、更新和状态管理的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "赛事管理")
@RestController
@RequestMapping("/api/competitions")
public class CompetitionController {

    @Autowired
    private ICompetitionService competitionService;

    /**
     * 查询赛事列表
     * 支持按年份和状态筛选
     *
     * @param current 当前页码
     * @param size 每页记录数
     * @param year 赛事年份
     * @param status 赛事状态
     * @return API响应（包含分页赛事列表）
     */
    @Operation(summary ="查询赛事列表")
    @GetMapping
    public ApiResponse<PageResponse<Competition>> listCompetitions(
            @Parameter(description ="当前页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description ="每页记录数") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description ="赛事年份") @RequestParam(required = false) Integer year,
            @Parameter(description ="赛事状态") @RequestParam(required = false) Competition.CompetitionStatus status) {

        // 构建查询条件
        QueryWrapper<Competition> queryWrapper = new QueryWrapper<>();

        // 添加年份条件
        if (year != null) {
            queryWrapper.eq("year", year);
        }

        // 添加状态条件
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc("create_time");

        // 执行分页查询
        Page<Competition> page = competitionService.page(new Page<>(current, size), queryWrapper);

        // 构建分页响应
        PageResponse<Competition> pageResponse = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );

        return ApiResponse.success(pageResponse);
    }

    /**
     * 查询赛事详情
     * 根据赛事ID查询赛事详细信息
     *
     * @param id 赛事ID
     * @return API响应（包含赛事详情）
     */
    @Operation(summary ="查询赛事详情")
    @GetMapping("/{id}")
    public ApiResponse<Competition> getCompetitionById(@Parameter(description ="赛事ID") @PathVariable Long id) {
        Competition competition = competitionService.getById(id);

        if (competition == null) {
            return ApiResponse.notFound("赛事不存在");
        }

        return ApiResponse.success(competition);
    }

    /**
     * 查询赛事赛道
     * 查询指定赛事的所有赛道信息
     *
     * @param id 赛事ID
     * @return API响应（包含赛道列表）
     */
    @Operation(summary ="查询赛事赛道")
    @GetMapping("/{id}/tracks")
    public ApiResponse<List<CompetitionTrack>> listCompetitionTracks(@Parameter(description ="赛事ID") @PathVariable Long id) {
        List<CompetitionTrack> tracks = competitionService.listCompetitionTracks(id);
        return ApiResponse.success(tracks);
    }

    /**
     * 创建赛事
     * 管理员创建新的赛事
     *
     * @param competition 赛事实体
     * @return API响应（包含创建的赛事）
     */
    @Operation(summary ="创建赛事")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Competition> createCompetition(@RequestBody Competition competition) {
        try {
            // 创建赛事
            Competition createdCompetition = competitionService.createCompetition(competition);

            log.info("赛事创建成功：{}", createdCompetition.getCompetitionName());

            return ApiResponse.success("赛事创建成功", createdCompetition);
        } catch (BusinessException e) {
            log.error("赛事创建失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新赛事
     * 管理员更新赛事信息
     *
     * @param id 赛事ID
     * @param competition 赛事实体（包含更新信息）
     * @return API响应
     */
    @Operation(summary ="更新赛事")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateCompetition(
            @Parameter(description ="赛事ID") @PathVariable Long id,
            @RequestBody Competition competition) {

        // 检查赛事是否存在
        Competition existingCompetition = competitionService.getById(id);
        if (existingCompetition == null) {
            return ApiResponse.notFound("赛事不存在");
        }

        // 设置赛事ID
        competition.setId(id);

        // 更新赛事信息
        boolean success = competitionService.updateById(competition);

        if (!success) {
            return ApiResponse.error("更新失败");
        }

        log.info("赛事更新成功：{}", id);

        return ApiResponse.success("更新成功");
    }

    /**
     * 发布赛事
     * 管理员发布赛事，使其可以被用户查看和报名
     *
     * @param id 赛事ID
     * @return API响应
     */
    @Operation(summary ="发布赛事")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> publishCompetition(@Parameter(description ="赛事ID") @PathVariable Long id) {
        try {
            boolean success = competitionService.publishCompetition(id);

            if (!success) {
                return ApiResponse.error("赛事发布失败");
            }

            log.info("赛事发布成功：{}", id);

            return ApiResponse.success("赛事发布成功");
        } catch (BusinessException e) {
            log.error("赛事发布失败：{}", id, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 开始赛事
     * 管理员开始赛事，进入提交阶段
     *
     * @param id 赛事ID
     * @return API响应
     */
    @Operation(summary ="开始赛事")
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> startCompetition(@Parameter(description ="赛事ID") @PathVariable Long id) {
        try {
            boolean success = competitionService.startCompetition(id);

            if (!success) {
                return ApiResponse.error("赛事开始失败");
            }

            log.info("赛事开始成功：{}", id);

            return ApiResponse.success("赛事已开始");
        } catch (BusinessException e) {
            log.error("赛事开始失败：{}", id, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 结束赛事
     * 管理员结束赛事，进入评审阶段
     *
     * @param id 赛事ID
     * @return API响应
     */
    @Operation(summary ="结束赛事")
    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> finishCompetition(@Parameter(description ="赛事ID") @PathVariable Long id) {
        try {
            boolean success = competitionService.finishCompetition(id);

            if (!success) {
                return ApiResponse.error("赛事结束失败");
            }

            log.info("赛事结束成功：{}", id);

            return ApiResponse.success("赛事已结束");
        } catch (BusinessException e) {
            log.error("赛事结束失败：{}", id, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询当前进行中的赛事
     *
     * @return API响应（包含进行中的赛事列表）
     */
    @Operation(summary ="查询当前进行中的赛事")
    @GetMapping("/ongoing")
    public ApiResponse<List<Competition>> listOngoingCompetitions() {
        List<Competition> ongoingCompetitions = competitionService.listOngoingCompetitions();
        return ApiResponse.success(ongoingCompetitions);
    }

    /**
     * 查询已发布的赛事
     *
     * @return API响应（包含已发布的赛事列表）
     */
    @Operation(summary ="查询已发布的赛事")
    @GetMapping("/published")
    public ApiResponse<List<Competition>> listPublishedCompetitions() {
        List<Competition> publishedCompetitions = competitionService.listPublishedCompetitions();
        return ApiResponse.success(publishedCompetitions);
    }
}