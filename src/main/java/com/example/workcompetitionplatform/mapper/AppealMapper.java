package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.Appeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 申诉Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface AppealMapper extends BaseMapper<Appeal> {

    /**
     * 根据提交ID查询申诉记录
     *
     * @param submissionId 提交ID
     * @return 申诉记录列表
     */
    List<Appeal> selectBySubmissionId(@Param("submissionId") Long submissionId);

    /**
     * 根据申诉人ID查询申诉记录
     *
     * @param appellantUserId 申诉人用户ID
     * @return 申诉记录列表
     */
    List<Appeal> selectByAppellantUserId(@Param("appellantUserId") Long appellantUserId);

    /**
     * 根据申诉状态查询申诉记录
     *
     * @param status 申诉状态
     * @return 申诉记录列表
     */
    List<Appeal> selectByStatus(@Param("status") Appeal.AppealStatus status);

    /**
     * 根据处理人ID查询申诉记录
     *
     * @param handlerUserId 处理人用户ID
     * @return 申诉记录列表
     */
    List<Appeal> selectByHandlerUserId(@Param("handlerUserId") Long handlerUserId);

    /**
     * 根据赛事ID查询申诉记录
     *
     * @param competitionId 赛事ID
     * @return 申诉记录列表
     */
    List<Appeal> selectByCompetitionId(@Param("competitionId") Long competitionId);
}