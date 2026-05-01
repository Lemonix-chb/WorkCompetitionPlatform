package com.example.workcompetitionplatform.dto;

import com.example.workcompetitionplatform.entity.CompetitionPhase;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Competition Time Status DTO
 * 用于前端展示赛事时间状态和倒计时
 */
@Data
public class CompetitionTimeStatusDTO {
    private Long competitionId;
    private String competitionName;
    private CompetitionPhase currentPhase;

    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime submissionStart;
    private LocalDateTime submissionEnd;
    private LocalDateTime reviewStart;
    private LocalDateTime reviewEnd;

    private Boolean canRegister;
    private Boolean canSubmit;

    private Long registrationDaysRemaining;
    private Long submissionDaysRemaining;
}