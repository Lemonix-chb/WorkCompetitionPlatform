package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.dto.StudentResultDTO;
import com.example.workcompetitionplatform.entity.*;
import com.example.workcompetitionplatform.mapper.*;
import com.example.workcompetitionplatform.service.IReviewResultService;
import com.example.workcompetitionplatform.service.ITeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评审结果服务实现类
 *
 * @author 陈海波
 * @since 2026-05-01
 */
@Service
public class ReviewResultServiceImpl extends ServiceImpl<ReviewResultMapper, ReviewResult> implements IReviewResultService {

    @Autowired
    private ReviewResultMapper reviewResultMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private CompetitionTrackMapper trackMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private AIReviewReportMapper aiReviewReportMapper;

    @Autowired
    private JudgeReviewMapper judgeReviewMapper;

    @Autowired
    private ITeamService teamService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<StudentResultDTO> getStudentResults(Long userId) {
        List<Team> teams = teamService.listUserTeams(userId);
        if (teams.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> teamIds = teams.stream().map(Team::getId).toList();
        LambdaQueryWrapper<ReviewResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ReviewResult::getTeamId, teamIds);
        List<ReviewResult> results = list(wrapper);

        if (results.isEmpty()) {
            return new ArrayList<>();
        }

        // Batch load all related data to avoid N+1 queries
        List<Long> submissionIds = results.stream().map(ReviewResult::getSubmissionId).distinct().toList();
        List<Long> teamIdsFromResults = results.stream().map(ReviewResult::getTeamId).distinct().toList();

        Map<Long, Submission> submissionMap = new HashMap<>();
        Map<Long, Work> workMap = new HashMap<>();
        Map<Long, CompetitionTrack> trackMap = new HashMap<>();
        Map<Long, Competition> competitionMap = new HashMap<>();
        Map<Long, Team> teamMap = new HashMap<>();

        // Batch query submissions
        List<Submission> submissions = submissionMapper.selectBatchIds(submissionIds);
        submissions.forEach(s -> submissionMap.put(s.getId(), s));

        // Batch query works
        List<Long> workIds = submissions.stream()
            .map(Submission::getWorkId)
            .filter(id -> id != null)
            .distinct()
            .toList();
        if (!workIds.isEmpty()) {
            List<Work> works = workMapper.selectBatchIds(workIds);
            works.forEach(w -> workMap.put(w.getId(), w));

            // Batch query tracks
            List<Long> trackIds = works.stream()
                .map(Work::getTrackId)
                .filter(id -> id != null)
                .distinct()
                .toList();
            if (!trackIds.isEmpty()) {
                List<CompetitionTrack> tracks = trackMapper.selectBatchIds(trackIds);
                tracks.forEach(t -> trackMap.put(t.getId(), t));

                // Batch query competitions
                List<Long> competitionIds = tracks.stream()
                    .map(CompetitionTrack::getCompetitionId)
                    .filter(id -> id != null)
                    .distinct()
                    .toList();
                if (!competitionIds.isEmpty()) {
                    List<Competition> competitions = competitionMapper.selectBatchIds(competitionIds);
                    competitions.forEach(c -> competitionMap.put(c.getId(), c));
                }
            }
        }

        // Batch query teams
        List<Team> teamsFromResults = teamMapper.selectBatchIds(teamIdsFromResults);
        teamsFromResults.forEach(t -> teamMap.put(t.getId(), t));

        // Convert using preloaded data
        return results.stream()
            .map(result -> convertToDTOWithBatch(result, submissionMap, workMap, trackMap, competitionMap, teamMap))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult calculateReviewResult(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在");
        }

        Work work = workMapper.selectById(submission.getWorkId());
        if (work == null) {
            throw new RuntimeException("作品不存在");
        }

        // 查询是否已有结果记录
        LambdaQueryWrapper<ReviewResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewResult::getSubmissionId, submissionId);
        ReviewResult result = getOne(wrapper);

        if (result == null) {
            result = new ReviewResult();
            result.setTeamId(submission.getTeamId());
            result.setSubmissionId(submissionId);
        }

        // 获取AI评分
        BigDecimal aiScore = BigDecimal.ZERO;
        LambdaQueryWrapper<AIReviewReport> aiWrapper = new LambdaQueryWrapper<>();
        aiWrapper.eq(AIReviewReport::getSubmissionId, submissionId);
        aiWrapper.orderByDesc(AIReviewReport::getCreateTime);
        aiWrapper.last("LIMIT 1");
        AIReviewReport aiReport = aiReviewReportMapper.selectOne(aiWrapper);

        if (aiReport != null && aiReport.getOverallScore() != null) {
            aiScore = aiReport.getOverallScore();
        }

        result.setAiScore(aiScore);
        result.setAiWeight(new BigDecimal("0.30")); // AI权重30%

        // 获取评委平均评分
        BigDecimal judgeAvgScore = BigDecimal.ZERO;
        LambdaQueryWrapper<JudgeReview> judgeWrapper = new LambdaQueryWrapper<>();
        judgeWrapper.eq(JudgeReview::getSubmissionId, submissionId);
        judgeWrapper.eq(JudgeReview::getStatus, JudgeReview.ReviewStatus.SUBMITTED);
        List<JudgeReview> judgeReviews = judgeReviewMapper.selectList(judgeWrapper);

        if (!judgeReviews.isEmpty()) {
            BigDecimal totalScore = BigDecimal.ZERO;
            int count = 0;

            for (JudgeReview review : judgeReviews) {
                if (review.getOverallScore() != null) {
                    totalScore = totalScore.add(review.getOverallScore());
                    count++;
                }
            }

            if (count > 0) {
                judgeAvgScore = totalScore.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
            }
        }

        result.setJudgeAvgScore(judgeAvgScore);
        result.setJudgeWeight(new BigDecimal("0.70")); // 评委权重70%

        // 计算最终得分
        BigDecimal finalScore = aiScore.multiply(result.getAiWeight())
                .add(judgeAvgScore.multiply(result.getJudgeWeight()))
                .setScale(2, RoundingMode.HALF_UP);

        result.setFinalScore(finalScore);

        // 根据得分确定奖项等级
        if (finalScore.compareTo(new BigDecimal("90")) >= 0) {
            result.setAwardLevel(ReviewResult.AwardLevel.FIRST);
        } else if (finalScore.compareTo(new BigDecimal("80")) >= 0) {
            result.setAwardLevel(ReviewResult.AwardLevel.SECOND);
        } else if (finalScore.compareTo(new BigDecimal("70")) >= 0) {
            result.setAwardLevel(ReviewResult.AwardLevel.THIRD);
        } else {
            result.setAwardLevel(ReviewResult.AwardLevel.NONE);
        }

        // 保存或更新结果
        saveOrUpdate(result);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTrackRankings(Long trackId) {
        // 查询该赛道下所有已产生结果的作品
        LambdaQueryWrapper<Work> workWrapper = new LambdaQueryWrapper<>();
        workWrapper.eq(Work::getTrackId, trackId);
        List<Work> works = workMapper.selectList(workWrapper);

        if (works.isEmpty()) {
            return;
        }

        // 获取作品的提交ID
        List<Long> workIds = works.stream().map(Work::getId).toList();
        LambdaQueryWrapper<Submission> subWrapper = new LambdaQueryWrapper<>();
        subWrapper.in(Submission::getWorkId, workIds);
        List<Submission> submissions = submissionMapper.selectList(subWrapper);

        if (submissions.isEmpty()) {
            return;
        }

        List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();

        // 查询这些提交的评审结果
        LambdaQueryWrapper<ReviewResult> resultWrapper = new LambdaQueryWrapper<>();
        resultWrapper.in(ReviewResult::getSubmissionId, submissionIds);
        resultWrapper.orderByDesc(ReviewResult::getFinalScore);
        List<ReviewResult> results = list(resultWrapper);

        // 按最终得分降序排名
        int rank = 1;
        BigDecimal lastScore = null;
        int sameRankCount = 0;

        for (int i = 0; i < results.size(); i++) {
            ReviewResult result = results.get(i);
            BigDecimal currentScore = result.getFinalScore();

            if (lastScore != null && currentScore.compareTo(lastScore) < 0) {
                rank += sameRankCount;
                sameRankCount = 1;
            } else if (lastScore == null) {
                sameRankCount = 1;
            } else {
                sameRankCount++;
            }

            result.setRankInTrack(rank);
            updateById(result);

            lastScore = currentScore;
        }
    }

    /**
     * 转换为DTO
     */
    private StudentResultDTO convertToDTO(ReviewResult result) {
        StudentResultDTO dto = new StudentResultDTO();
        dto.setId(result.getId());
        dto.setSubmissionId(result.getSubmissionId()); // 添加提交ID，用于查询AI评审详情

        // 获取提交信息
        Submission submission = submissionMapper.selectById(result.getSubmissionId());
        if (submission != null) {
            dto.setSubmissionTime(submission.getSubmissionTime() != null ?
                    submission.getSubmissionTime().toString() : null);

            // 获取作品信息
            Work work = workMapper.selectById(submission.getWorkId());
            if (work != null) {
                dto.setWorkId(work.getId());
                dto.setWorkName(work.getWorkName());
                dto.setWorkCode(work.getWorkCode());
                dto.setWorkType(work.getWorkType().name());
                dto.setWorkTypeText(getWorkTypeText(work.getWorkType()));

                // 获取赛道信息
                CompetitionTrack track = trackMapper.selectById(work.getTrackId());
                if (track != null) {
                    dto.setTrackName(track.getTrackName());

                    // 获取赛事信息
                    Competition competition = competitionMapper.selectById(work.getCompetitionId());
                    if (competition != null) {
                        dto.setCompetitionName(competition.getCompetitionName());
                    }
                }
            }
        }

        // 获取团队信息
        Team team = teamMapper.selectById(result.getTeamId());
        if (team != null) {
            dto.setTeamName(team.getTeamName());
        }

        // 设置评分信息
        dto.setAiScore(result.getAiScore());
        dto.setJudgeAvgScore(result.getJudgeAvgScore());
        dto.setFinalScore(result.getFinalScore());

        // 设置奖项信息
        if (result.getAwardLevel() != null) {
            dto.setAwardLevel(result.getAwardLevel().name());
            dto.setAwardLevelText(getAwardLevelText(result.getAwardLevel()));
        }

        dto.setRankInTrack(result.getRankInTrack());
        dto.setFinalComment(result.getFinalComment());
        dto.setCreateTime(result.getCreateTime() != null ? result.getCreateTime().toString() : null);

        return dto;
    }

    private StudentResultDTO convertToDTOWithBatch(
            ReviewResult result,
            Map<Long, Submission> submissionMap,
            Map<Long, Work> workMap,
            Map<Long, CompetitionTrack> trackMap,
            Map<Long, Competition> competitionMap,
            Map<Long, Team> teamMap) {

        StudentResultDTO dto = new StudentResultDTO();
        dto.setId(result.getId());
        dto.setSubmissionId(result.getSubmissionId());

        Submission submission = submissionMap.get(result.getSubmissionId());
        if (submission != null) {
            dto.setSubmissionTime(submission.getSubmissionTime() != null ?
                submission.getSubmissionTime().toString() : null);

            Work work = workMap.get(submission.getWorkId());
            if (work != null) {
                dto.setWorkId(work.getId());
                dto.setWorkName(work.getWorkName());
                dto.setWorkCode(work.getWorkCode());
                dto.setWorkType(work.getWorkType() != null ? work.getWorkType().name() : null);
                dto.setWorkTypeText(work.getWorkType() != null ? getWorkTypeText(work.getWorkType()) : null);

                CompetitionTrack track = trackMap.get(work.getTrackId());
                if (track != null) {
                    dto.setTrackName(track.getTrackName());

                    Competition competition = competitionMap.get(track.getCompetitionId());
                    if (competition != null) {
                        dto.setCompetitionName(competition.getCompetitionName());
                    }
                }
            }
        }

        Team team = teamMap.get(result.getTeamId());
        if (team != null) {
            dto.setTeamName(team.getTeamName());
        }

        dto.setAiScore(result.getAiScore());
        dto.setJudgeAvgScore(result.getJudgeAvgScore());
        dto.setFinalScore(result.getFinalScore());

        if (result.getAwardLevel() != null) {
            dto.setAwardLevel(result.getAwardLevel().name());
            dto.setAwardLevelText(getAwardLevelText(result.getAwardLevel()));
        }

        dto.setRankInTrack(result.getRankInTrack());
        dto.setFinalComment(result.getFinalComment());
        dto.setCreateTime(result.getCreateTime() != null ? result.getCreateTime().toString() : null);

        return dto;
    }

    /**
     * 作品类型文本转换
     */
    private String getWorkTypeText(Work.WorkType workType) {
        switch (workType) {
            case CODE:
                return "程序设计";
            case PPT:
                return "演示文稿";
            case VIDEO:
                return "数媒视频";
            default:
                return workType.name();
        }
    }

    /**
     * 奖项等级文本转换
     */
    private String getAwardLevelText(ReviewResult.AwardLevel awardLevel) {
        switch (awardLevel) {
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
                return awardLevel.name();
        }
    }
}