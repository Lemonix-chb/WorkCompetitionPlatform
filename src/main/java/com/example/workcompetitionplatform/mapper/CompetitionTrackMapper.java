package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.CompetitionTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛道Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface CompetitionTrackMapper extends BaseMapper<CompetitionTrack> {

    /**
     * 根据赛事ID查询所有赛道
     *
     * @param competitionId 赛事ID
     * @return 赛道列表
     */
    List<CompetitionTrack> selectByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 根据赛道编码查询赛道
     *
     * @param trackCode 赛道编码
     * @return 赛道实体
     */
    CompetitionTrack selectByTrackCode(@Param("trackCode") String trackCode);
}