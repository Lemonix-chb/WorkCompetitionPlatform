package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.TeamApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队申请Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface TeamApplicationMapper extends BaseMapper<TeamApplication> {

    /**
     * 根据团队ID查询所有申请
     *
     * @param teamId 团队ID
     * @return 团队申请列表
     */
    List<TeamApplication> selectByTeamId(@Param("teamId") Long teamId);

    /**
     * 根据团队ID查询待处理的申请（包含申请人姓名）
     *
     * @param teamId 团队ID
     * @return 团队申请列表（包含申请人详细信息）
     */
    List<TeamApplication> selectApplicationsWithDetails(@Param("teamId") Long teamId);

    /**
     * 根据申请人ID查询所有申请
     *
     * @param applicantUserId 申请人ID
     * @return 团队申请列表
     */
    List<TeamApplication> selectByApplicantUserId(@Param("applicantUserId") Long applicantUserId);

    /**
     * 根据申请人ID查询申请列表（包含团队名称）
     *
     * @param applicantId 申请人ID
     * @return 团队申请列表（包含团队详细信息）
     */
    List<TeamApplication> selectMyApplicationsWithDetails(@Param("applicantId") Long applicantId);

    /**
     * 根据申请状态查询申请列表
     *
     * @param status 申请状态
     * @return 团队申请列表
     */
    List<TeamApplication> selectByStatus(@Param("status") TeamApplication.ApplicationStatus status);

    /**
     * 根据团队ID和申请人ID查询申请记录
     *
     * @param teamId 团队ID
     * @param applicantUserId 申请人ID
     * @return 团队申请实体
     */
    TeamApplication selectByTeamIdAndApplicantUserId(@Param("teamId") Long teamId, @Param("applicantUserId") Long applicantUserId);
}