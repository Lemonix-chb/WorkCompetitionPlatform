package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.Appeal;
import com.example.workcompetitionplatform.entity.Submission;
import com.example.workcompetitionplatform.mapper.AppealMapper;
import com.example.workcompetitionplatform.mapper.SubmissionMapper;
import com.example.workcompetitionplatform.service.IAppealService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 申诉服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements IAppealService {

    private final SubmissionMapper submissionMapper;

    public AppealServiceImpl(SubmissionMapper submissionMapper) {
        this.submissionMapper = submissionMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appeal submitAppeal(Long submissionId, Long appellantUserId, String appealReason) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在");
        }

        // 检查是否存在未处理的申诉
        if (hasPendingAppeal(submissionId)) {
            throw new RuntimeException("该提交已存在未处理的申诉");
        }

        // 创建申诉记录
        Appeal appeal = new Appeal();
        appeal.setSubmissionId(submissionId);
        appeal.setAppellantId(appellantUserId);
        appeal.setAppealContent(appealReason);
        appeal.setStatus(Appeal.AppealStatus.PENDING);
        appeal.setSubmitTime(LocalDateTime.now());

        // 保存申诉记录
        save(appeal);

        return appeal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAppeal(Long appealId, Long handlerUserId, boolean accepted, String handlingResult) {
        Appeal appeal = getById(appealId);
        if (appeal == null) {
            throw new RuntimeException("申诉不存在");
        }

        // 检查申诉状态
        if (appeal.getStatus() != Appeal.AppealStatus.PENDING) {
            throw new RuntimeException("申诉已被处理");
        }

        // 更新申诉状态
        appeal.setProcessorId(handlerUserId);
        appeal.setStatus(accepted ? Appeal.AppealStatus.ACCEPTED : Appeal.AppealStatus.REJECTED);
        appeal.setProcessResult(handlingResult);
        appeal.setProcessTime(LocalDateTime.now());

        return updateById(appeal);
    }

    @Override
    public List<Appeal> listBySubmissionId(Long submissionId) {
        return baseMapper.selectBySubmissionId(submissionId);
    }

    @Override
    public List<Appeal> listByAppellantUserId(Long appellantUserId) {
        return baseMapper.selectByAppellantUserId(appellantUserId);
    }

    @Override
    public List<Appeal> listByStatus(Appeal.AppealStatus status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<Appeal> listByCompetitionId(Long competitionId) {
        return baseMapper.selectByCompetitionId(competitionId);
    }

    @Override
    public List<Appeal> listPendingAppeals() {
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appeal::getStatus, Appeal.AppealStatus.PENDING)
                .orderByDesc(Appeal::getSubmitTime);
        return list(wrapper);
    }

    @Override
    public boolean hasPendingAppeal(Long submissionId) {
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appeal::getSubmissionId, submissionId)
                .eq(Appeal::getStatus, Appeal.AppealStatus.PENDING);
        return count(wrapper) > 0;
    }
}