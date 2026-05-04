package com.example.workcompetitionplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.dto.AdminStatsDTO;
import com.example.workcompetitionplatform.dto.SystemStatsDTO;
import com.example.workcompetitionplatform.entity.Competition;
import com.example.workcompetitionplatform.entity.JudgeReview;
import com.example.workcompetitionplatform.entity.Team;
import com.example.workcompetitionplatform.entity.CompetitionTrack;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.entity.UserRole;
import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.entity.User;
import com.example.workcompetitionplatform.entity.ReviewResult;
import com.example.workcompetitionplatform.mapper.CompetitionMapper;
import com.example.workcompetitionplatform.mapper.CompetitionTrackMapper;
import com.example.workcompetitionplatform.mapper.JudgeReviewMapper;
import com.example.workcompetitionplatform.mapper.SubmissionMapper;
import com.example.workcompetitionplatform.mapper.TeamMapper;
import com.example.workcompetitionplatform.mapper.UserRoleMapper;
import com.example.workcompetitionplatform.mapper.WorkMapper;
import com.example.workcompetitionplatform.mapper.UserMapper;
import com.example.workcompetitionplatform.mapper.ReviewResultMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;

/**
 * 管理员控制器
 * 提供管理员相关的统计和功能接口
 *
 * @author 陈海波
 * @since 2026-04-11
 */
@Slf4j
@Tag(name = "管理员功能")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private CompetitionTrackMapper competitionTrackMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private JudgeReviewMapper judgeReviewMapper;

    @Autowired
    private ReviewResultMapper reviewResultMapper;

    /**
     * 查询管理员统计数据
     * 返回系统的整体统计信息（包含图表数据）
     *
     * @return API响应（包含统计数据）
     */
    @Operation(summary = "查询管理员统计数据")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminStatsDTO> getAdminStats() {
        try {
            AdminStatsDTO stats = new AdminStatsDTO();

            // 1. 基础统计
            stats.setTotalCompetitions(competitionMapper.selectCount(null).intValue());
            stats.setTotalTeams(teamMapper.selectCount(null).intValue());
            stats.setTotalWorks(workMapper.selectCount(null).intValue());
            stats.setTotalUsers(userMapper.selectCount(null).intValue());

            // 2. 进行中的赛事数
            LambdaQueryWrapper<Competition> activeCompWrapper = new LambdaQueryWrapper<>();
            activeCompWrapper.eq(Competition::getStatus, Competition.CompetitionStatus.ONGOING);
            stats.setActiveCompetitions(competitionMapper.selectCount(activeCompWrapper).intValue());

            // 3. 本周新增团队数
            LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
            LambdaQueryWrapper<Team> newTeamWrapper = new LambdaQueryWrapper<>();
            newTeamWrapper.ge(Team::getCreateTime, weekStart);
            stats.setNewTeamsThisWeek(teamMapper.selectCount(newTeamWrapper).intValue());

            // 4. 作品状态统计
            LambdaQueryWrapper<Work> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.in(Work::getDevelopmentStatus,
                Work.DevelopmentStatus.IN_PROGRESS, Work.DevelopmentStatus.COMPLETED);
            stats.setPendingWorks(workMapper.selectCount(pendingWrapper).intValue());

            LambdaQueryWrapper<Work> reviewingWrapper = new LambdaQueryWrapper<>();
            reviewingWrapper.eq(Work::getDevelopmentStatus, Work.DevelopmentStatus.SUBMITTED);
            stats.setReviewingWorks(workMapper.selectCount(reviewingWrapper).intValue());

            LambdaQueryWrapper<Work> awardedWrapper = new LambdaQueryWrapper<>();
            awardedWrapper.eq(Work::getDevelopmentStatus, Work.DevelopmentStatus.AWARDED);
            stats.setAwardedWorks(workMapper.selectCount(awardedWrapper).intValue());

            // 已完成评审数
            LambdaQueryWrapper<JudgeReview> completedReviewWrapper = new LambdaQueryWrapper<>();
            completedReviewWrapper.eq(JudgeReview::getStatus, JudgeReview.ReviewStatus.SUBMITTED);
            stats.setCompletedReviews(judgeReviewMapper.selectCount(completedReviewWrapper).intValue());

            // 5. 用户角色统计
            // 管理员数量（roleId=1）
            LambdaQueryWrapper<UserRole> adminWrapper = new LambdaQueryWrapper<>();
            adminWrapper.eq(UserRole::getRoleId, 1L);
            int adminCount = userRoleMapper.selectCount(adminWrapper).intValue();

            // 评委数量（roleId=2）
            LambdaQueryWrapper<UserRole> judgeWrapper = new LambdaQueryWrapper<>();
            judgeWrapper.eq(UserRole::getRoleId, 2L);
            stats.setTotalJudges(userRoleMapper.selectCount(judgeWrapper).intValue());

            // 学生数量（roleId=3）
            LambdaQueryWrapper<UserRole> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.eq(UserRole::getRoleId, 3L);
            stats.setTotalStudents(userRoleMapper.selectCount(studentWrapper).intValue());

            // 总用户数 = 管理员 + 评委 + 学生
            stats.setTotalUsers(adminCount + stats.getTotalJudges() + stats.getTotalStudents());

            // 6. 作品类型统计
            LambdaQueryWrapper<Work> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(Work::getWorkType, Work.WorkType.CODE);
            stats.setCodeWorks(workMapper.selectCount(codeWrapper).intValue());

            LambdaQueryWrapper<Work> pptWrapper = new LambdaQueryWrapper<>();
            pptWrapper.eq(Work::getWorkType, Work.WorkType.PPT);
            stats.setPptWorks(workMapper.selectCount(pptWrapper).intValue());

            LambdaQueryWrapper<Work> videoWrapper = new LambdaQueryWrapper<>();
            videoWrapper.eq(Work::getWorkType, Work.WorkType.VIDEO);
            stats.setVideoWorks(workMapper.selectCount(videoWrapper).intValue());

            // 7. AI审核统计
            LambdaQueryWrapper<Submission> aiValidWrapper = new LambdaQueryWrapper<>();
            aiValidWrapper.eq(Submission::getStatus, Submission.SubmissionStatus.VALID);
            stats.setAiValidCount(submissionMapper.selectCount(aiValidWrapper).intValue());

            LambdaQueryWrapper<Submission> aiInvalidWrapper = new LambdaQueryWrapper<>();
            aiInvalidWrapper.eq(Submission::getStatus, Submission.SubmissionStatus.INVALID);
            stats.setAiInvalidCount(submissionMapper.selectCount(aiInvalidWrapper).intValue());

            // 8. 作品提交趋势（近7天）
            List<String> dates = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

            for (int i = 6; i >= 0; i--) {
                LocalDateTime dayStart = LocalDateTime.now().minusDays(i).toLocalDate().atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1);

                LambdaQueryWrapper<Work> dayWrapper = new LambdaQueryWrapper<>();
                dayWrapper.ge(Work::getCreateTime, dayStart).lt(Work::getCreateTime, dayEnd);
                int dayCount = workMapper.selectCount(dayWrapper).intValue();

                dates.add(dayStart.format(formatter));
                counts.add(dayCount);
            }
            stats.setSubmissionTrend(new AdminStatsDTO.SubmissionTrend(dates, counts));

            log.info("查询管理员统计数据成功");

            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("查询管理员统计数据失败", e);
            return ApiResponse.error("查询统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 查询系统详细统计数据
     * 返回管理员数据统计页面的详细统计信息
     *
     * @return API响应（包含详细统计数据）
     */
    @Operation(summary = "查询系统详细统计数据")
    @GetMapping("/system-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemStatsDTO> getSystemStats() {
        // 统计赛事总数
        Long competitionCount = competitionMapper.selectCount(null);

        // 统计赛道总数
        Long trackCount = competitionTrackMapper.selectCount(null);

        // 统计团队总数
        Long teamCount = teamMapper.selectCount(null);

        // 统计作品总数
        Long workCount = workMapper.selectCount(null);

        // 统计提交总数
        Long submissionCount = submissionMapper.selectCount(null);

        // 统计用户总数
        Long userCount = userMapper.selectCount(null);

        // 统计管理员用户数（roleId=1）
        QueryWrapper<UserRole> adminWrapper = new QueryWrapper<>();
        adminWrapper.eq("role_id", 1);
        Long adminCount = userRoleMapper.selectCount(adminWrapper);

        // 统计评委用户数（roleId=2）
        QueryWrapper<UserRole> judgeWrapper = new QueryWrapper<>();
        judgeWrapper.eq("role_id", 2);
        Long judgeCount = userRoleMapper.selectCount(judgeWrapper);

        // 统计学生用户数（roleId=3）
        QueryWrapper<UserRole> studentWrapper = new QueryWrapper<>();
        studentWrapper.eq("role_id", 3);
        Long studentCount = userRoleMapper.selectCount(studentWrapper);

        // 统计已完成评审数
        QueryWrapper<JudgeReview> completedReviewWrapper = new QueryWrapper<>();
        completedReviewWrapper.in("status", "SUBMITTED", "CONFIRMED");
        Long completedReviews = judgeReviewMapper.selectCount(completedReviewWrapper);

        // 统计待评审数
        QueryWrapper<JudgeReview> pendingReviewWrapper = new QueryWrapper<>();
        pendingReviewWrapper.eq("status", "DRAFT");
        Long pendingReviews = judgeReviewMapper.selectCount(pendingReviewWrapper);

        // 统计已设置奖项数
        QueryWrapper<Work> awardedWrapper = new QueryWrapper<>();
        awardedWrapper.eq("development_status", "AWARDED");
        Long awardedCount = workMapper.selectCount(awardedWrapper);

        // 统计学院分布（查询学生用户的学院分布）
        Map<String, Integer> collegeDistribution = new HashMap<>();
        QueryWrapper<User> collegeWrapper = new QueryWrapper<>();
        collegeWrapper.isNotNull("college");
        collegeWrapper.eq("status", "ACTIVE");
        List<User> usersWithCollege = userMapper.selectList(collegeWrapper);
        for (User user : usersWithCollege) {
            String college = user.getCollege();
            if (college != null && !college.isEmpty()) {
                collegeDistribution.merge(college, 1, Integer::sum);
            }
        }

        // 统计专业分布（查询学生用户的专业分布）
        Map<String, Integer> majorDistribution = new HashMap<>();
        QueryWrapper<User> majorWrapper = new QueryWrapper<>();
        majorWrapper.isNotNull("major");
        majorWrapper.eq("status", "ACTIVE");
        List<User> usersWithMajor = userMapper.selectList(majorWrapper);
        for (User user : usersWithMajor) {
            String major = user.getMajor();
            if (major != null && !major.isEmpty()) {
                majorDistribution.merge(major, 1, Integer::sum);
            }
        }

        // 统计奖项等级分布（从ReviewResult表查询）
        Map<String, Integer> awardLevelDistribution = new HashMap<>();
        QueryWrapper<ReviewResult> awardWrapper = new QueryWrapper<>();
        awardWrapper.isNotNull("award_level");
        List<ReviewResult> reviewResults = reviewResultMapper.selectList(awardWrapper);
        for (ReviewResult result : reviewResults) {
            ReviewResult.AwardLevel level = result.getAwardLevel();
            if (level != null) {
                String levelName = getAwardLevelName(level);
                awardLevelDistribution.merge(levelName, 1, Integer::sum);
            }
        }

        // 构建详细统计数据DTO
        SystemStatsDTO stats = new SystemStatsDTO();
        stats.setTotalCompetitions(competitionCount.intValue());
        stats.setTotalTracks(trackCount.intValue());
        stats.setTotalTeams(teamCount.intValue());
        stats.setTotalWorks(workCount.intValue());
        stats.setTotalSubmissions(submissionCount.intValue());
        stats.setTotalUsers(userCount.intValue());
        stats.setStudentCount(studentCount.intValue());
        stats.setJudgeCount(judgeCount.intValue());
        stats.setAdminCount(adminCount.intValue());
        stats.setCompletedReviews(completedReviews.intValue());
        stats.setPendingReviews(pendingReviews.intValue());
        stats.setAwardedCount(awardedCount.intValue());
        stats.setCollegeDistribution(collegeDistribution);
        stats.setMajorDistribution(majorDistribution);
        stats.setAwardLevelDistribution(awardLevelDistribution);

        log.info("查询系统详细统计数据：赛事 {}，赛道 {}，团队 {}，作品 {}，提交 {}，用户 {}（学生 {}，评委 {}，管理员 {}），评审完成 {}，待评审 {}，获奖 {}",
                competitionCount, trackCount, teamCount, workCount, submissionCount,
                userCount, studentCount, judgeCount, adminCount,
                completedReviews, pendingReviews, awardedCount);

        return ApiResponse.success(stats);
    }

    /**
     * 将奖项等级枚举转换为中文显示名称
     *
     * @param level 奖项等级枚举
     * @return 奖项等级中文名称
     */
    private String getAwardLevelName(ReviewResult.AwardLevel level) {
        if (level == null) {
            return "未设置";
        }
        switch (level) {
            case FIRST:
                return "一等奖";
            case SECOND:
                return "二等奖";
            case THIRD:
                return "三等奖";
            case EXCELLENCE:
                return "优秀奖";
            case NONE:
                return "无奖项";
            default:
                return level.toString();
        }
    }
}