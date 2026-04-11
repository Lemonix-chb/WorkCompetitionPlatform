package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.entity.Appeal;
import com.example.workcompetitionplatform.exception.BusinessException;
import com.example.workcompetitionplatform.service.IAppealService;
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
 * 申诉管理控制器
 * 提供申诉提交、查询和处理的接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "申诉管理")
@RestController
@RequestMapping("/api/appeals")
public class AppealController {

    @Autowired
    private IAppealService appealService;

    /**
     * 提交申诉
     * 用户对评审结果提交申诉
     *
     * @param submissionId 提交ID
     * @param appealReason 申诉理由
     * @return API响应（包含申诉记录）
     */
    @Operation(summary ="提交申诉")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ApiResponse<Appeal> submitAppeal(
            @Parameter(description ="提交ID") @RequestParam Long submissionId,
            @Parameter(description ="申诉理由") @RequestParam String appealReason) {

        try {
            // 获取当前用户ID作为申诉人
            Long appellantUserId = UserContext.getCurrentUserId();

            // 提交申诉
            Appeal appeal = appealService.submitAppeal(submissionId, appellantUserId, appealReason);

            log.info("申诉提交成功：用户 {} -> 提交 {}", appellantUserId, submissionId);

            return ApiResponse.success("申诉已提交", appeal);
        } catch (BusinessException e) {
            log.error("申诉提交失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询申诉详情
     * 根据申诉ID查询申诉详细信息
     *
     * @param id 申诉ID
     * @return API响应（包含申诉详情）
     */
    @Operation(summary ="查询申诉详情")
    @GetMapping("/{id}")
    public ApiResponse<Appeal> getAppealById(@Parameter(description ="申诉ID") @PathVariable Long id) {
        Appeal appeal = appealService.getById(id);

        if (appeal == null) {
            return ApiResponse.notFound("申诉不存在");
        }

        return ApiResponse.success(appeal);
    }

    /**
     * 查询我的申诉
     * 查询当前用户提交的所有申诉记录
     *
     * @return API响应（包含申诉列表）
     */
    @Operation(summary ="查询我的申诉")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Appeal>> listMyAppeals() {
        Long userId = UserContext.getCurrentUserId();
        List<Appeal> appeals = appealService.listByAppellantUserId(userId);
        return ApiResponse.success(appeals);
    }

    /**
     * 处理申诉
     * 管理员处理用户提交的申诉
     *
     * @param id 申诉ID
     * @param accepted 是否接受申诉
     * @param handlingResult 处理结果说明
     * @return API响应
     */
    @Operation(summary ="处理申诉")
    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> handleAppeal(
            @Parameter(description ="申诉ID") @PathVariable Long id,
            @Parameter(description ="是否接受申诉") @RequestParam boolean accepted,
            @Parameter(description ="处理结果说明") @RequestParam(required = false) String handlingResult) {

        try {
            // 获取当前用户ID作为处理人
            Long handlerUserId = UserContext.getCurrentUserId();

            // 处理申诉
            boolean success = appealService.handleAppeal(id, handlerUserId, accepted, handlingResult);

            if (!success) {
                return ApiResponse.error("申诉处理失败");
            }

            log.info("申诉处理成功：{} -> {}", id, accepted ? "接受" : "拒绝");

            return ApiResponse.success("申诉已处理");
        } catch (BusinessException e) {
            log.error("申诉处理失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询待处理的申诉列表
     * 管理员查询所有待处理的申诉
     *
     * @return API响应（包含申诉列表）
     */
    @Operation(summary ="查询待处理的申诉列表")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Appeal>> listPendingAppeals() {
        List<Appeal> appeals = appealService.listPendingAppeals();
        return ApiResponse.success(appeals);
    }

    /**
     * 查询作品的申诉列表
     * 查询指定作品的所有申诉记录
     *
     * @param submissionId 提交ID
     * @return API响应（包含申诉列表）
     */
    @Operation(summary ="查询作品的申诉列表")
    @GetMapping("/submission/{submissionId}")
    public ApiResponse<List<Appeal>> listAppealsBySubmission(@Parameter(description ="提交ID") @PathVariable Long submissionId) {
        List<Appeal> appeals = appealService.listBySubmissionId(submissionId);
        return ApiResponse.success(appeals);
    }

    /**
     * 查询赛事的申诉列表
     * 管理员查询指定赛事的所有申诉记录
     *
     * @param competitionId 赛事ID
     * @return API响应（包含申诉列表）
     */
    @Operation(summary ="查询赛事的申诉列表")
    @GetMapping("/competition/{competitionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Appeal>> listAppealsByCompetition(@Parameter(description ="赛事ID") @PathVariable Long competitionId) {
        List<Appeal> appeals = appealService.listByCompetitionId(competitionId);
        return ApiResponse.success(appeals);
    }

    /**
     * 检查提交是否存在未处理的申诉
     * 检查指定作品是否存在未处理的申诉
     *
     * @param submissionId 提交ID
     * @return API响应
     */
    @Operation(summary ="检查提交是否存在未处理的申诉")
    @GetMapping("/pending/{submissionId}")
    public ApiResponse<Boolean> hasPendingAppeal(@Parameter(description ="提交ID") @PathVariable Long submissionId) {
        boolean hasPending = appealService.hasPendingAppeal(submissionId);
        return ApiResponse.success(hasPending);
    }
}