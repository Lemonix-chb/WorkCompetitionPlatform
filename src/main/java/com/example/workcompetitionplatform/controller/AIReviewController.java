package com.example.workcompetitionplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.dto.AIReviewCallbackRequest;
import com.example.workcompetitionplatform.dto.AIReviewDetailDTO;
import com.example.workcompetitionplatform.entity.AIReviewReport;
import com.example.workcompetitionplatform.entity.AIReviewDetail;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.entity.TeamMember;
import com.example.workcompetitionplatform.entity.Notification;
import com.example.workcompetitionplatform.mapper.SubmissionMapper;
import com.example.workcompetitionplatform.mapper.TeamMemberMapper;
import com.example.workcompetitionplatform.mapper.AIReviewReportMapper;
import com.example.workcompetitionplatform.mapper.AIReviewDetailMapper;
import com.example.workcompetitionplatform.mapper.NotificationMapper;
import com.example.workcompetitionplatform.service.IReviewService;
import com.example.workcompetitionplatform.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI评审控制器
 * 提供AI初审相关的API接口
 *
 * @author 陈海波
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/api/ai-reviews")
@Tag(name = "AI评审管理", description = "AI初审相关接口")
public class AIReviewController {

    @Autowired
    private IReviewService reviewService;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private AIReviewReportMapper aiReviewReportMapper;

    @Autowired
    private AIReviewDetailMapper aiReviewDetailMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 执行AI初审
     *
     * @param submissionId 提交ID
     * @return AI评审报告
     */
    @PostMapping("/perform/{submissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "执行AI初审", description = "对指定提交的作品执行AI初审")
    public ApiResponse<AIReviewReport> performAIReview(
            @Parameter(description = "提交ID") @PathVariable Long submissionId) {
        try {
            AIReviewReport report = reviewService.performAIReview(submissionId);
            return ApiResponse.success(report);
        } catch (Exception e) {
            return ApiResponse.error(500, "AI评审失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI评审报告
     *
     * @param submissionId 提交ID
     * @return AI评审报告
     */
    @GetMapping("/report/{submissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JUDGE', 'STUDENT')")
    @Operation(summary = "获取AI评审报告", description = "根据提交ID查询AI评审报告")
    public ApiResponse<AIReviewReport> getAIReviewReport(
            @Parameter(description = "提交ID") @PathVariable Long submissionId) {
        AIReviewReport report = reviewService.getAIReviewReport(submissionId);
        if (report == null) {
            return ApiResponse.error(404, "AI评审报告不存在");
        }
        return ApiResponse.success(report);
    }

    /**
     * 获取AI评审详情列表
     *
     * @param reportId 报告ID
     * @return 评审详情列表
     */
    @GetMapping("/details/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JUDGE', 'STUDENT')")
    @Operation(summary = "获取AI评审详情", description = "根据报告ID查询评审详情列表")
    public ApiResponse<List<AIReviewDetail>> getAIReviewDetails(
            @Parameter(description = "报告ID") @PathVariable Long reportId) {
        List<AIReviewDetail> details = reviewService.listAIReviewDetails(reportId);
        return ApiResponse.success(details);
    }

    /**
     * 查询作品的AI评审状态
     * 学生可查看自己作品的评审进度
     *
     * @param workId 作品ID
     * @return 评审状态信息
     */
    @GetMapping("/status/{workId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询AI评审状态", description = "学生查看自己作品的AI评审进度")
    public ApiResponse<Map<String, Object>> getAIReviewStatus(
            @Parameter(description = "作品ID") @PathVariable Long workId) {

        // 查询作品的提交记录
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getWorkId, workId);
        Submission submission = submissionMapper.selectOne(wrapper);

        if (submission == null) {
            return ApiResponse.error(404, "作品未提交");
        }

        // 验证用户是作品团队成员
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getTeamId, submission.getTeamId());
        memberWrapper.eq(TeamMember::getUserId, userId);
        TeamMember member = teamMemberMapper.selectOne(memberWrapper);
        if (member == null) {
            return ApiResponse.error(403, "无权查看此作品的评审状态");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", submission.getStatus().name());
        result.put("validationResult", submission.getValidationResult());
        result.put("submissionId", submission.getId());

        // 如果评审完成，返回报告概要
        if (submission.getStatus() == Submission.SubmissionStatus.VALID
                || submission.getStatus() == Submission.SubmissionStatus.INVALID) {
            AIReviewReport report = reviewService.getAIReviewReport(submission.getId());
            if (report != null) {
                result.put("overallScore", report.getOverallScore());
                result.put("riskLevel", report.getRiskLevel() != null ? report.getRiskLevel().name() : null);
                result.put("reviewSummary", report.getReviewSummary());
                result.put("duplicateRate", report.getDuplicateRate());
            }
        }

        return ApiResponse.success(result);
    }

    /**
     * 获取作品的完整AI评审报告
     * 学生查看详细评审结果
     *
     * @param workId 作品ID
     * @return 完整AI评审报告
     */
    @GetMapping("/full-report/{workId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "获取完整AI评审报告", description = "学生查看作品的完整AI评审报告")
    public ApiResponse<AIReviewReport> getFullReportByWorkId(
            @Parameter(description = "作品ID") @PathVariable Long workId) {

        // 查询作品的提交记录
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getWorkId, workId);
        Submission submission = submissionMapper.selectOne(wrapper);

        if (submission == null) {
            return ApiResponse.error(404, "作品未提交");
        }

        // 验证用户是作品团队成员
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getTeamId, submission.getTeamId());
        memberWrapper.eq(TeamMember::getUserId, userId);
        TeamMember member = teamMemberMapper.selectOne(memberWrapper);
        if (member == null) {
            return ApiResponse.error(403, "无权查看此作品的评审报告");
        }

        // 检查评审是否完成
        if (submission.getStatus() != Submission.SubmissionStatus.VALID
                && submission.getStatus() != Submission.SubmissionStatus.INVALID) {
            return ApiResponse.error(400, "AI评审尚未完成");
        }

        AIReviewReport report = reviewService.getAIReviewReport(submission.getId());
        if (report == null) {
            return ApiResponse.error(404, "AI评审报告不存在");
        }

        return ApiResponse.success(report);
    }

    /**
     * Python Agent回调接口
     * 接收AI评审完成的报告，保存到数据库并更新状态
     *
     * @param request AI评审回调请求
     * @return 操作结果
     */
    @PostMapping("/callback")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "Python Agent回调接口", description = "接收AI评审完成的报告")
    public ApiResponse<Void> receiveAIReviewCallback(
            @Parameter(description = "AI评审回调请求") @RequestBody AIReviewCallbackRequest request) throws Exception {

        try {
            // 1. 提取报告数据
            AIReviewCallbackRequest.AIReviewReportDTO reportDTO = request.getReport();
            Long submissionId = request.getSubmissionId();

            // 2. 创建并保存AIReviewReport
            AIReviewReport report = new AIReviewReport();
            report.setSubmissionId(submissionId);

            // 获取提交信息以查询teamId
            Submission submission = submissionMapper.selectById(submissionId);
            if (submission == null) {
                return ApiResponse.error(404, "提交记录不存在");
            }
            report.setTeamId(submission.getTeamId());

            // 设置评分
            report.setOverallScore(reportDTO.getOverallScore());
            report.setInnovationScore(reportDTO.getInnovationScore());
            report.setPracticalityScore(reportDTO.getPracticalityScore());
            report.setUserExperienceScore(reportDTO.getUserExperienceScore());
            report.setDocumentationScore(reportDTO.getDocumentationScore());

            // 设置代码质量指标（如果有）
            if (reportDTO.getDuplicateRate() != null) {
                report.setDuplicateRate(reportDTO.getDuplicateRate());
            }
            if (reportDTO.getCodeQualityScore() != null) {
                report.setCodeQualityScore(reportDTO.getCodeQualityScore());
            }

            // 设置文本内容
            report.setReviewSummary(reportDTO.getReviewSummary());

            // 将建议列表转换为字符串
            if (reportDTO.getImprovementSuggestions() != null && !reportDTO.getImprovementSuggestions().isEmpty()) {
                String suggestions = String.join("\n", reportDTO.getImprovementSuggestions());
                report.setImprovementSuggestions(suggestions);
            }

            report.setAiModel(reportDTO.getAiModel());

            // 设置风险等级
            try {
                report.setRiskLevel(AIReviewReport.RiskLevel.valueOf(reportDTO.getRiskLevel()));
            } catch (IllegalArgumentException e) {
                report.setRiskLevel(AIReviewReport.RiskLevel.LOW);
            }

            // 保存报告
            aiReviewReportMapper.insert(report);

            // 保存详细评审项到ai_review_detail表
            Long reportId = report.getId();

            // 保存四个评分维度（PPT作品：创意、视觉效果、内容呈现、原创性）
            if (reportDTO.getInnovationScore() != null &&
                reportDTO.getInnovationScore().compareTo(BigDecimal.ZERO) > 0) {
                AIReviewDetail innovationDetail = new AIReviewDetail();
                innovationDetail.setReportId(reportId);
                innovationDetail.setCheckItem("创意评分");
                innovationDetail.setCheckResult("作品在内容创意和视觉设计创新方面的表现");
                innovationDetail.setScore(reportDTO.getInnovationScore());
                aiReviewDetailMapper.insert(innovationDetail);
            }

            if (reportDTO.getUserExperienceScore() != null &&
                reportDTO.getUserExperienceScore().compareTo(BigDecimal.ZERO) > 0) {
                AIReviewDetail visualDetail = new AIReviewDetail();
                visualDetail.setReportId(reportId);
                visualDetail.setCheckItem("视觉效果评分");
                visualDetail.setCheckResult("排版布局、色彩搭配和图文比例的表现");
                visualDetail.setScore(reportDTO.getUserExperienceScore());
                aiReviewDetailMapper.insert(visualDetail);
            }

            if (reportDTO.getDocumentationScore() != null &&
                reportDTO.getDocumentationScore().compareTo(BigDecimal.ZERO) > 0) {
                AIReviewDetail contentDetail = new AIReviewDetail();
                contentDetail.setReportId(reportId);
                contentDetail.setCheckItem("内容呈现评分");
                contentDetail.setCheckResult("逻辑结构、信息密度和章节划分的表现");
                contentDetail.setScore(reportDTO.getDocumentationScore());
                aiReviewDetailMapper.insert(contentDetail);
            }

            if (reportDTO.getPracticalityScore() != null &&
                reportDTO.getPracticalityScore().compareTo(BigDecimal.ZERO) > 0) {
                AIReviewDetail originalityDetail = new AIReviewDetail();
                originalityDetail.setReportId(reportId);
                originalityDetail.setCheckItem("原创性评分");
                originalityDetail.setCheckResult("原创元素使用、原创内容占比的表现");
                originalityDetail.setScore(reportDTO.getPracticalityScore());
                aiReviewDetailMapper.insert(originalityDetail);
            }

            // 保存作品亮点
            if (reportDTO.getStrengths() != null) {
                for (String strength : reportDTO.getStrengths()) {
                    AIReviewDetail detail = new AIReviewDetail();
                    detail.setReportId(reportId);
                    detail.setCheckItem("作品亮点");
                    detail.setCheckResult(strength);
                    detail.setScore(BigDecimal.ZERO);
                    aiReviewDetailMapper.insert(detail);
                }
            }

            // 保存不足之处
            if (reportDTO.getWeaknesses() != null) {
                for (String weakness : reportDTO.getWeaknesses()) {
                    AIReviewDetail detail = new AIReviewDetail();
                    detail.setReportId(reportId);
                    detail.setCheckItem("不足之处");
                    detail.setCheckResult(weakness);
                    detail.setScore(BigDecimal.ZERO);
                    aiReviewDetailMapper.insert(detail);
                }
            }

            // 保存改进建议
            if (reportDTO.getImprovementSuggestions() != null) {
                for (String suggestion : reportDTO.getImprovementSuggestions()) {
                    AIReviewDetail detail = new AIReviewDetail();
                    detail.setReportId(reportId);
                    detail.setCheckItem("改进建议");
                    detail.setCheckResult(suggestion);
                    detail.setScore(BigDecimal.ZERO);
                    aiReviewDetailMapper.insert(detail);
                }
            }

            // 3. 更新Submission状态
            Submission submissionToUpdate = submissionMapper.selectById(submissionId);
            if (submissionToUpdate == null) {
                return ApiResponse.error(404, "提交记录不存在，无法更新状态");
            }

            System.out.println("========================================");
            System.out.println("[AIReviewController] 准备更新Submission状态");
            System.out.println("  - submissionId: " + submissionId);
            System.out.println("  - 当前状态: " + submissionToUpdate.getStatus());
            System.out.println("  - riskLevel: " + report.getRiskLevel());

            if (report.getRiskLevel() == AIReviewReport.RiskLevel.HIGH) {
                submissionToUpdate.setStatus(Submission.SubmissionStatus.INVALID);
                System.out.println("  - 新状态: INVALID (高风险)");
            } else {
                submissionToUpdate.setStatus(Submission.SubmissionStatus.VALID);
                System.out.println("  - 新状态: VALID (低风险)");
            }

            int updateResult = submissionMapper.updateById(submissionToUpdate);
            System.out.println("  - updateById结果: " + updateResult + " (1表示成功)");

            // 验证更新是否成功
            Submission verifySubmission = submissionMapper.selectById(submissionId);
            System.out.println("  - 验证更新后状态: " + verifySubmission.getStatus());
            System.out.println("========================================");

            // 4. 发送通知给团队成员
            LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(TeamMember::getTeamId, submission.getTeamId());
            List<TeamMember> members = teamMemberMapper.selectList(memberWrapper);

            for (TeamMember member : members) {
                Notification notification = new Notification();
                notification.setUserId(member.getUserId());
                notification.setTitle("AI评审完成通知");
                notification.setNotificationType(Notification.NotificationType.SYSTEM);

                String statusText = submission.getStatus() == Submission.SubmissionStatus.VALID ? "通过" : "未通过";
                String content = String.format(
                    "您的作品AI评审已完成，综合得分%.1f分，评审结果：%s。%s",
                    report.getOverallScore(),
                    statusText,
                    report.getRiskLevel() == AIReviewReport.RiskLevel.HIGH ? "请改进后重新提交。" : ""
                );

                notification.setContent(content);
                notificationMapper.insert(notification);
            }

            return ApiResponse.success();

        } catch (Exception e) {
            System.out.println("========================================");
            System.out.println("[AIReviewController] 回调处理异常！");
            System.out.println("  - 异常类型: " + e.getClass().getName());
            System.out.println("  - 异常信息: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            // 重新抛出异常，让@Transactional回滚整个事务
            // 避免报告已保存但submission状态未更新的不一致问题
            throw e;
        }
    }

    /**
     * 根据提交ID获取AI评审报告（供评委使用）
     *
     * @param submissionId 提交ID
     * @return AI评审报告
     */
    @GetMapping("/judge-report/{submissionId}")
    @PreAuthorize("hasRole('JUDGE')")
    @Operation(summary = "评委获取AI评审报告", description = "评委评审时查看AI评审结果作为参考")
    public ApiResponse<AIReviewReport> getJudgeAIReviewReport(
            @Parameter(description = "提交ID") @PathVariable Long submissionId) {

        AIReviewReport report = reviewService.getAIReviewReport(submissionId);
        if (report == null) {
            return ApiResponse.success(null); // 没有报告时返回null，不影响评审流程
        }

        return ApiResponse.success(report);
    }

    /**
     * 根据提交ID获取AI评审详情（供学生使用）
     * 返回完整的AI评审报告，包含解析后的亮点、不足、建议列表
     *
     * @param submissionId 提交ID
     * @return AI评审详情DTO
     */
    @GetMapping("/detail/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "学生获取AI评审详情", description = "学生查看完整的AI评审报告，包含亮点、不足、建议")
    public ApiResponse<AIReviewDetailDTO> getAIReviewDetailBySubmissionId(
            @Parameter(description = "提交ID") @PathVariable Long submissionId) {

        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return ApiResponse.error(404, "提交记录不存在");
        }

        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getTeamId, submission.getTeamId());
        memberWrapper.eq(TeamMember::getUserId, userId);
        TeamMember member = teamMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return ApiResponse.error(403, "无权查看此作品的AI评审报告");
        }

        if (submission.getStatus() != Submission.SubmissionStatus.VALID
                && submission.getStatus() != Submission.SubmissionStatus.INVALID) {
            return ApiResponse.error(400, "AI评审尚未完成");
        }

        AIReviewDetailDTO detailDTO = reviewService.getAIReviewDetailDTO(submissionId, submission);
        if (detailDTO == null) {
            return ApiResponse.error(404, "AI评审报告不存在");
        }

        return ApiResponse.success(detailDTO);
    }
}