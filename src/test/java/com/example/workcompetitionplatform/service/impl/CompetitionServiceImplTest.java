package com.example.workcompetitionplatform.service.impl;

import com.example.workcompetitionplatform.dto.CompetitionTimeStatusDTO;
import com.example.workcompetitionplatform.entity.Competition;
import com.example.workcompetitionplatform.entity.CompetitionPhase;
import com.example.workcompetitionplatform.mapper.CompetitionMapper;
import com.example.workcompetitionplatform.util.DateTimeConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CompetitionServiceImpl 时间状态测试
 * 测试时间阶段判定和边界情况
 *
 * @author 陈海波
 * @since 2026-05-01
 */
class CompetitionServiceImplTest {

    @Mock
    private CompetitionMapper competitionMapper;

    @InjectMocks
    private CompetitionServiceImpl competitionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("测试：报名未开始阶段")
    void testGetTimeStatus_BeforeRegistration() {
        Competition competition = createTestCompetition();
        competition.setRegistrationStart(DateTimeConstants.now().plusDays(10));
        competition.setRegistrationEnd(DateTimeConstants.now().plusDays(30));

        when(competitionMapper.selectById(1L)).thenReturn(competition);

        CompetitionTimeStatusDTO status = competitionService.getTimeStatus(1L);

        assertEquals(CompetitionPhase.BEFORE_REGISTRATION, status.getCurrentPhase());
        assertFalse(status.getCanRegister());
        assertEquals(10L, status.getRegistrationDaysRemaining());
    }

    @Test
    @DisplayName("测试：报名进行中阶段")
    void testGetTimeStatus_InRegistration() {
        Competition competition = createTestCompetition();
        competition.setRegistrationStart(DateTimeConstants.now().minusDays(5));
        competition.setRegistrationEnd(DateTimeConstants.now().plusDays(10));

        when(competitionMapper.selectById(1L)).thenReturn(competition);

        CompetitionTimeStatusDTO status = competitionService.getTimeStatus(1L);

        assertEquals(CompetitionPhase.REGISTRATION, status.getCurrentPhase());
        assertTrue(status.getCanRegister());
        assertEquals(10L, status.getRegistrationDaysRemaining());
    }

    @Test
    @DisplayName("测试：报名已截止，提交未开始")
    void testGetTimeStatus_BeforeSubmission() {
        Competition competition = createTestCompetition();
        competition.setRegistrationStart(DateTimeConstants.now().minusDays(30));
        competition.setRegistrationEnd(DateTimeConstants.now().minusDays(10));
        competition.setSubmissionStart(DateTimeConstants.now().plusDays(5));
        competition.setSubmissionEnd(DateTimeConstants.now().plusDays(20));

        when(competitionMapper.selectById(1L)).thenReturn(competition);

        CompetitionTimeStatusDTO status = competitionService.getTimeStatus(1L);

        assertEquals(CompetitionPhase.BEFORE_SUBMISSION, status.getCurrentPhase());
        assertFalse(status.getCanRegister());
        assertFalse(status.getCanSubmit());
        assertEquals(5L, status.getSubmissionDaysRemaining());
    }

    @Test
    @DisplayName("测试：提交进行中阶段")
    void testGetTimeStatus_InSubmission() {
        Competition competition = createTestCompetition();
        competition.setRegistrationStart(DateTimeConstants.now().minusDays(30));
        competition.setRegistrationEnd(DateTimeConstants.now().minusDays(10));
        competition.setSubmissionStart(DateTimeConstants.now().minusDays(5));
        competition.setSubmissionEnd(DateTimeConstants.now().plusDays(10));

        when(competitionMapper.selectById(1L)).thenReturn(competition);

        CompetitionTimeStatusDTO status = competitionService.getTimeStatus(1L);

        assertEquals(CompetitionPhase.SUBMISSION, status.getCurrentPhase());
        assertFalse(status.getCanRegister());
        assertTrue(status.getCanSubmit());
        assertEquals(10L, status.getSubmissionDaysRemaining());
    }

    @Test
    @DisplayName("测试：评审进行中阶段")
    void testGetTimeStatus_InReview() {
        Competition competition = createTestCompetition();
        competition.setRegistrationStart(DateTimeConstants.now().minusDays(60));
        competition.setRegistrationEnd(DateTimeConstants.now().minusDays(40));
        competition.setSubmissionStart(DateTimeConstants.now().minusDays(35));
        competition.setSubmissionEnd(DateTimeConstants.now().minusDays(10));
        competition.setReviewStart(DateTimeConstants.now().minusDays(5));
        competition.setReviewEnd(DateTimeConstants.now().plusDays(20));

        when(competitionMapper.selectById(1L)).thenReturn(competition);

        CompetitionTimeStatusDTO status = competitionService.getTimeStatus(1L);

        assertEquals(CompetitionPhase.REVIEW, status.getCurrentPhase());
        assertFalse(status.getCanRegister());
        assertFalse(status.getCanSubmit());
    }

    @Test
    @DisplayName("测试：赛事已结束")
    void testGetTimeStatus_Finished() {
        Competition competition = createTestCompetition();
        competition.setRegistrationStart(DateTimeConstants.now().minusDays(90));
        competition.setRegistrationEnd(DateTimeConstants.now().minusDays(70));
        competition.setSubmissionStart(DateTimeConstants.now().minusDays(65));
        competition.setSubmissionEnd(DateTimeConstants.now().minusDays(40));
        competition.setReviewStart(DateTimeConstants.now().minusDays(35));
        competition.setReviewEnd(DateTimeConstants.now().minusDays(10));

        when(competitionMapper.selectById(1L)).thenReturn(competition);

        CompetitionTimeStatusDTO status = competitionService.getTimeStatus(1L);

        assertEquals(CompetitionPhase.FINISHED, status.getCurrentPhase());
        assertFalse(status.getCanRegister());
        assertFalse(status.getCanSubmit());
    }

    @Test
    @DisplayName("测试：恰好在报名截止边界")
    void testGetTimeStatus_ExactlyAtRegistrationEnd() {
        Competition competition = createTestCompetition();
        LocalDateTime now = DateTimeConstants.now();
        competition.setRegistrationStart(now.minusDays(10));
        competition.setRegistrationEnd(now); // 恰好现在结束
        competition.setSubmissionStart(now.plusDays(5));
        competition.setSubmissionEnd(now.plusDays(20));

        when(competitionMapper.selectById(1L)).thenReturn(competition);

        CompetitionTimeStatusDTO status = competitionService.getTimeStatus(1L);

        // 截止时间恰好当前时间，不在报名期内
        assertEquals(CompetitionPhase.BEFORE_SUBMISSION, status.getCurrentPhase());
        assertFalse(status.getCanRegister());
    }

    @Test
    @DisplayName("测试：剩余天数为负数（已过截止期）")
    void testGetTimeStatus_NegativeDaysRemaining() {
        Competition competition = createTestCompetition();
        LocalDateTime now = DateTimeConstants.now();
        competition.setRegistrationStart(now.minusDays(30));
        competition.setRegistrationEnd(now.minusDays(10)); // 已过期10天

        when(competitionMapper.selectById(1L)).thenReturn(competition);

        CompetitionTimeStatusDTO status = competitionService.getTimeStatus(1L);

        // 过期后不在报名期
        assertFalse(status.getCanRegister());
    }

    @Test
    @DisplayName("测试：赛事不存在抛出异常")
    void testGetTimeStatus_CompetitionNotFound() {
        when(competitionMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            competitionService.getTimeStatus(999L);
        });
    }

    /**
     * 创建测试用的赛事对象
     */
    private Competition createTestCompetition() {
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setCompetitionName("测试赛事");
        competition.setCompetitionYear(2026);
        return competition;
    }
}