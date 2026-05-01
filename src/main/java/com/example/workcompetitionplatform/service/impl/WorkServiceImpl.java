package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.AIReviewReport;
import com.example.workcompetitionplatform.entity.Competition;
import com.example.workcompetitionplatform.entity.JudgeReview;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.entity.Team;
import com.example.workcompetitionplatform.entity.TeamMember;
import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.entity.WorkAttachment;
import com.example.workcompetitionplatform.mapper.AIReviewReportMapper;
import com.example.workcompetitionplatform.mapper.CompetitionMapper;
import com.example.workcompetitionplatform.mapper.JudgeReviewMapper;
import com.example.workcompetitionplatform.mapper.SubmissionMapper;
import com.example.workcompetitionplatform.mapper.TeamMapper;
import com.example.workcompetitionplatform.mapper.TeamMemberMapper;
import com.example.workcompetitionplatform.mapper.WorkAttachmentMapper;
import com.example.workcompetitionplatform.mapper.WorkMapper;
import com.example.workcompetitionplatform.service.IWorkService;
import com.example.workcompetitionplatform.service.AsyncAIReviewService;
import com.example.workcompetitionplatform.exception.BusinessException;
import com.example.workcompetitionplatform.util.DateTimeConstants;
import com.example.workcompetitionplatform.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 作品服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work> implements IWorkService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private WorkAttachmentMapper workAttachmentMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private JudgeReviewMapper judgeReviewMapper;

    @Autowired
    private AIReviewReportMapper aiReviewReportMapper;

    @Autowired
    private AsyncAIReviewService asyncAIReviewService;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Work createWork(String workName, Long teamId, Long competitionId, Long trackId, Work.WorkType workType) {
        // 检查团队是否存在
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("团队不存在");
        }

        // 检查团队状态是否允许创建作品（CONFIRMED或REGISTERED状态都可以）
        if (team.getStatus() != Team.TeamStatus.CONFIRMED && team.getStatus() != Team.TeamStatus.REGISTERED) {
            String statusMsg = team.getStatus() == Team.TeamStatus.FORMING ? "组建中" : team.getStatus().toString();
            throw new BusinessException("团队当前状态为\"" + statusMsg + "\"，无法创建作品。请先确认团队或完成报名");
        }

        // 检查团队是否已有作品
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getTeamId, teamId);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该团队已创建作品，不能重复创建。如需修改请在作品管理中编辑");
        }

        // 创建作品
        Work work = new Work();
        work.setWorkCode(generateWorkCode());
        work.setWorkName(workName);
        work.setTeamId(teamId);
        work.setCompetitionId(competitionId);
        work.setTrackId(trackId);
        work.setWorkType(workType);
        work.setDevelopmentStatus(Work.DevelopmentStatus.IN_PROGRESS);
        work.setCurrentVersion(1);

        // 保存作品
        save(work);

        return work;
    }

    @Override
    public Work getByWorkCode(String workCode) {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getWorkCode, workCode);
        return getOne(wrapper);
    }

    @Override
    public List<Work> listByTeamId(Long teamId) {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getTeamId, teamId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Work> listByTrackId(Long trackId) {
        return baseMapper.selectByCompetitionTrackId(trackId);
    }

    @Override
    public List<Work> listByCompetitionId(Long competitionId) {
        return baseMapper.selectByCompetitionId(competitionId);
    }

    @Override
    public List<Work> listUserWorks(Long userId) {
        // 查询用户所在的团队
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getUserId, userId);
        List<TeamMember> members = teamMemberMapper.selectList(memberWrapper);

        if (members.isEmpty()) {
            return List.of();
        }

        // 获取团队ID列表
        List<Long> teamIds = members.stream()
                .map(TeamMember::getTeamId)
                .toList();

        // 查询团队的作品
        LambdaQueryWrapper<Work> workWrapper = new LambdaQueryWrapper<>();
        workWrapper.in(Work::getTeamId, teamIds);
        return list(workWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWorkInfo(Long workId, String workName, String description, String innovationPoints,
                                  String keyFeatures, String techStack, String divisionOfLabor, String targetUsers) {
        Work work = getById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在");
        }

        // 已获奖的作品不能修改
        if (work.getDevelopmentStatus() == Work.DevelopmentStatus.AWARDED) {
            throw new BusinessException("已获奖的作品无法修改");
        }

        // 更新作品信息
        work.setWorkName(workName);
        work.setDescription(description);
        work.setInnovationPoints(innovationPoints);
        work.setKeyFeatures(keyFeatures);
        work.setTechStack(techStack);
        work.setDivisionOfLabor(divisionOfLabor);
        work.setTargetUsers(targetUsers);

        // 如果作品已提交，更新后状态变为已完成（需要重新提交）
        if (work.getDevelopmentStatus() == Work.DevelopmentStatus.SUBMITTED) {
            work.setDevelopmentStatus(Work.DevelopmentStatus.COMPLETED);
        }

        return updateById(work);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitWork(Long workId) {
        Work work = getById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在");
        }

        // 检查是否在提交期内（一次性加载competition避免重复查询）
        Competition competition = competitionMapper.selectById(work.getCompetitionId());
        LocalDateTime now = DateTimeConstants.now();
        if (!now.isAfter(competition.getSubmissionStart()) ||
            !now.isBefore(competition.getSubmissionEnd())) {
            String endTime = competition.getSubmissionEnd()
                .format(DateTimeConstants.STANDARD_FORMAT);
            throw new BusinessException("作品提交已截止，截止时间：" + endTime);
        }

        // 检查作品状态（开发中、已完成或已提交状态都可以提交）
        if (work.getDevelopmentStatus() == Work.DevelopmentStatus.AWARDED) {
            throw new BusinessException("已获奖的作品无法重新提交");
        }

        // 检查作品是否有附件
        List<WorkAttachment> attachments = workAttachmentMapper.selectByWorkId(workId);
        if (attachments.isEmpty()) {
            throw new BusinessException("作品必须上传附件才能提交");
        }

        // 获取最新的附件作为提交文件
        WorkAttachment latestAttachment = attachments.get(attachments.size() - 1);

        // 查询是否已存在submission记录
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getWorkId, workId);
        Submission existingSubmission = submissionMapper.selectOne(wrapper);

        Long submissionId;
        if (existingSubmission != null) {
            existingSubmission.setValidationResult(null);
            LambdaQueryWrapper<JudgeReview> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.eq(JudgeReview::getSubmissionId, existingSubmission.getId());
            judgeReviewMapper.delete(reviewWrapper);

            LambdaQueryWrapper<AIReviewReport> aiReportWrapper = new LambdaQueryWrapper<>();
            aiReportWrapper.eq(AIReviewReport::getSubmissionId, existingSubmission.getId());
            aiReviewReportMapper.delete(aiReportWrapper);

            populateSubmissionFields(existingSubmission, latestAttachment, work);
            submissionMapper.updateById(existingSubmission);
            submissionId = existingSubmission.getId();
        } else {
            Submission submission = new Submission();
            submission.setWorkId(workId);
            submission.setTeamId(work.getTeamId());
            submission.setSubmissionCode(work.getWorkCode());
            submission.setSubmitterId(work.getTeamId());
            submission.setFileType(Submission.FileType.valueOf(work.getWorkType().name()));
            submission.setIsFinalVersion(true);
            populateSubmissionFields(submission, latestAttachment, work);
            submissionMapper.insert(submission);
            submissionId = submission.getId();
        }

        asyncAIReviewService.performAIReviewAsync(submissionId, UserContext.getCurrentUserId());

        // 更新作品状态为已提交
        work.setDevelopmentStatus(Work.DevelopmentStatus.SUBMITTED);
        return updateById(work);
    }

    @Override
    public Work getWorkDetail(Long workId) {
        Work work = getById(workId);
        if (work == null) {
            return null;
        }

        // 查询团队信息
        Team team = teamMapper.selectById(work.getTeamId());
        if (team != null) {
            // 可以在这里添加额外的团队信息到work对象中
            // 如果需要返回团队信息，可以考虑使用DTO
        }

        return work;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markWorkComplete(Long workId, Long userId) {
        Work work = getById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在");
        }

        // 验证用户是否是团队成员
        Team team = teamMapper.selectById(work.getTeamId());
        if (team == null) {
            throw new BusinessException("团队不存在");
        }

        // 检查用户是否是团队成员（队长或成员）
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, work.getTeamId())
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, TeamMember.MemberStatus.ACTIVE);

        TeamMember member = teamMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException("只有团队成员可以标记作品完成");
        }

        // 检查作品当前状态
        if (work.getDevelopmentStatus() != Work.DevelopmentStatus.IN_PROGRESS) {
            throw new BusinessException("只有开发中的作品可以标记为完成");
        }

        // 更新作品状态为已完成
        work.setDevelopmentStatus(Work.DevelopmentStatus.COMPLETED);
        return updateById(work);
    }

    @Override
    public List<Work> list() {
        List<Work> works = baseMapper.selectAllWorks();
        return works != null ? works : new java.util.ArrayList<>();
    }

    /**
     * 生成作品编号
     * 格式：WORK-YYYYMMDD-UUID8
     */
    private String generateWorkCode() {
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "WORK-" + dateStr + "-" + uuid;
    }

    private void populateSubmissionFields(Submission submission, WorkAttachment attachment, Work work) {
        submission.setFileName(attachment.getFileName());
        submission.setFilePath(attachment.getFilePath());
        submission.setFileSizeMb(BigDecimal.valueOf(attachment.getFileSize() / (1024.0 * 1024.0)));
        submission.setVersion(work.getCurrentVersion());
        submission.setStatus(Submission.SubmissionStatus.VALIDATING);
        submission.setSubmissionTime(DateTimeConstants.now());
    }
}