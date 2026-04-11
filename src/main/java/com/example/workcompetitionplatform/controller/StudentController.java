package com.example.workcompetitionplatform.controller;

import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.dto.StudentStatsDTO;
import com.example.workcompetitionplatform.entity.TeamMember;
import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.mapper.TeamMemberMapper;
import com.example.workcompetitionplatform.mapper.WorkMapper;
import com.example.workcompetitionplatform.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生控制器
 * 提供学生相关的统计和功能接口
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@Tag(name = "学生功能")
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private WorkMapper workMapper;

    /**
     * 查询学生统计数据
     * 返回学生的赛事报名、团队参与、作品提交、获奖数量等统计信息
     *
     * @return API响应（包含统计数据）
     */
    @Operation(summary = "查询学生统计数据")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<StudentStatsDTO> getStudentStats() {
        Long userId = UserContext.getCurrentUserId();

        // 统计赛事报名数量（通过团队参与的赛事）
        List<TeamMember> teamMembers = teamMemberMapper.selectByUserId(userId);
        int competitionCount = (int) teamMembers.stream()
                .map(TeamMember::getTeamId)
                .distinct()
                .count();

        // 统计团队参与数量
        int teamCount = teamMembers.size();

        // 统计作品提交数量
        List<Work> works = workMapper.selectByUserId(userId);
        int workCount = works.size();

        // 统计获奖数量
        int awardCount = (int) works.stream()
                .filter(work -> work.getDevelopmentStatus() == Work.DevelopmentStatus.AWARDED)
                .count();

        // 构建统计数据DTO
        StudentStatsDTO stats = new StudentStatsDTO();
        stats.setCompetitionCount(competitionCount);
        stats.setTeamCount(teamCount);
        stats.setWorkCount(workCount);
        stats.setAwardCount(awardCount);

        log.info("查询学生统计数据：用户ID {}", userId);

        return ApiResponse.success(stats);
    }
}