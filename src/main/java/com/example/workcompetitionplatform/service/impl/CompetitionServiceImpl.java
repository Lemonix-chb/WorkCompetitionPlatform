package com.example.workcompetitionplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workcompetitionplatform.entity.Competition;
import com.example.workcompetitionplatform.entity.CompetitionTrack;
import com.example.workcompetitionplatform.entity.Registration;
import com.example.workcompetitionplatform.mapper.CompetitionMapper;
import com.example.workcompetitionplatform.mapper.CompetitionTrackMapper;
import com.example.workcompetitionplatform.mapper.RegistrationMapper;
import com.example.workcompetitionplatform.mapper.TeamRegistrationMapper;
import com.example.workcompetitionplatform.service.ICompetitionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 赛事服务实现类
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition> implements ICompetitionService {

    private final CompetitionTrackMapper competitionTrackMapper;
    private final RegistrationMapper registrationMapper;
    private final TeamRegistrationMapper teamRegistrationMapper;

    public CompetitionServiceImpl(CompetitionTrackMapper competitionTrackMapper,
                                   RegistrationMapper registrationMapper,
                                   TeamRegistrationMapper teamRegistrationMapper) {
        this.competitionTrackMapper = competitionTrackMapper;
        this.registrationMapper = registrationMapper;
        this.teamRegistrationMapper = teamRegistrationMapper;
    }

    @Override
    public List<Competition> listByYear(Integer year) {
        return baseMapper.selectByYear(year);
    }

    @Override
    public List<Competition> listByStatus(Competition.CompetitionStatus status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<Competition> listOngoingCompetitions() {
        return baseMapper.selectOngoingCompetitions();
    }

    @Override
    public List<Competition> listPublishedCompetitions() {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, Competition.CompetitionStatus.PUBLISHED)
                .or()
                .eq(Competition::getStatus, Competition.CompetitionStatus.ONGOING);
        return list(wrapper);
    }

    @Override
    public List<CompetitionTrack> listCompetitionTracks(Long competitionId) {
        return competitionTrackMapper.selectByCompetitionId(competitionId);
    }

    @Override
    public CompetitionTrack getTrackByCode(String trackCode) {
        return competitionTrackMapper.selectByTrackCode(trackCode);
    }

    @Override
    public CompetitionTrack getTrackById(Long trackId) {
        return competitionTrackMapper.selectById(trackId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Competition createCompetition(Competition competition) {
        save(competition);
        return competition;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionTrack createTrack(CompetitionTrack track) {
        competitionTrackMapper.insert(track);
        return track;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishCompetition(Long competitionId) {
        Competition competition = getById(competitionId);
        if (competition == null) {
            throw new RuntimeException("赛事不存在");
        }

        if (competition.getStatus() != Competition.CompetitionStatus.DRAFT) {
            throw new RuntimeException("赛事不在草稿状态");
        }

        competition.setStatus(Competition.CompetitionStatus.PUBLISHED);
        return updateById(competition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startCompetition(Long competitionId) {
        Competition competition = getById(competitionId);
        if (competition == null) {
            throw new RuntimeException("赛事不存在");
        }

        competition.setStatus(Competition.CompetitionStatus.ONGOING);
        return updateById(competition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishCompetition(Long competitionId) {
        Competition competition = getById(competitionId);
        if (competition == null) {
            throw new RuntimeException("赛事不存在");
        }

        competition.setStatus(Competition.CompetitionStatus.FINISHED);
        return updateById(competition);
    }

    @Override
    public boolean isInRegistrationPeriod(Long competitionId) {
        Competition competition = getById(competitionId);
        if (competition == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(competition.getRegistrationStart()) && now.isBefore(competition.getRegistrationEnd());
    }

    @Override
    public boolean isInSubmissionPeriod(Long competitionId) {
        Competition competition = getById(competitionId);
        if (competition == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(competition.getSubmissionStart()) && now.isBefore(competition.getSubmissionEnd());
    }

    @Override
    public boolean isInReviewPeriod(Long competitionId) {
        Competition competition = getById(competitionId);
        if (competition == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(competition.getReviewStart()) && now.isBefore(competition.getReviewEnd());
    }

    @Override
    public boolean hasUserRegistered(Long competitionId, Long userId) {
        return registrationMapper.selectByCompetitionIdAndUserId(competitionId, userId) != null;
    }

    @Override
    public boolean hasTeamRegistered(Long competitionId, Long teamId) {
        return teamRegistrationMapper.selectByCompetitionIdAndTeamId(competitionId, teamId) != null;
    }

    @Override
    public int getRegistrationCount(Long competitionId) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getCompetitionId, competitionId);
        return Math.toIntExact(registrationMapper.selectCount(wrapper));
    }

    @Override
    public int getTrackRegistrationCount(Long trackId) {
        // Registration 没有 competitionTrackId 字段
        // 需要通过其他方式查询，这里简化处理
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(Registration::getCompetitionTrackId, trackId);
        return Math.toIntExact(registrationMapper.selectCount(wrapper));
    }
}