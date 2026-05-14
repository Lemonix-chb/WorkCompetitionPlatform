package com.example.workcompetitionplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workcompetitionplatform.dto.StudentResultDTO;
import com.example.workcompetitionplatform.entity.ReviewResult;

import java.util.List;

/**
 * 评审结果服务接口
 *
 * @author 陈海波
 * @since 2026-05-01
 */
public interface IReviewResultService extends IService<ReviewResult> {

    /**
     * 查询学生的评审结果列表
     *
     * @param userId 学生用户ID
     * @return 评审结果DTO列表
     */
    List<StudentResultDTO> getStudentResults(Long userId);

    /**
     * 计算并生成评审结果
     * 根据AI评分和评委评分计算最终得分
     *
     * @param submissionId 提交ID
     * @return 评审结果
     */
    ReviewResult calculateReviewResult(Long submissionId);

    /**
     * 更新赛道排名
     * 根据最终得分更新同一赛道内的排名
     *
     * @param trackId 赛道ID
     */
    void updateTrackRankings(Long trackId);
}