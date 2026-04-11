package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.Competition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {

    /**
     * 根据赛事年份查询赛事列表
     *
     * @param competitionYear 赛事年份
     * @return 赛事列表
     */
    List<Competition> selectByYear(@Param("competitionYear") Integer competitionYear);

    /**
     * 根据赛事状态查询赛事列表
     *
     * @param status 赛事状态
     * @return 赛事列表
     */
    List<Competition> selectByStatus(@Param("status") Competition.CompetitionStatus status);

    /**
     * 查询当前进行中的赛事
     *
     * @return 当前进行中的赛事列表
     */
    List<Competition> selectOngoingCompetitions();
}