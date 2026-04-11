package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workcompetitionplatform.entity.WorkAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作品附件Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface WorkAttachmentMapper extends BaseMapper<WorkAttachment> {

    /**
     * 根据作品ID查询所有附件
     *
     * @param workId 作品ID
     * @return 附件列表
     */
    List<WorkAttachment> selectByWorkId(@Param("workId") Long workId);

    /**
     * 根据作品ID和附件类型查询附件
     *
     * @param workId 作品ID
     * @param attachmentType 附件类型
     * @return 附件列表
     */
    List<WorkAttachment> selectByWorkIdAndType(@Param("workId") Long workId, @Param("attachmentType") WorkAttachment.AttachmentType attachmentType);
}