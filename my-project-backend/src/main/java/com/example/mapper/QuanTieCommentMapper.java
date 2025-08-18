package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.QuanTieComment;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:25
 */
@Mapper
public interface QuanTieCommentMapper extends BaseMapper<QuanTieComment> {

    /**
     * 获取指定评论的回复
     *
     * @param commentIds
     * @param count
     * @return
     */
    @MapKey("firstLevelCommonId")
    List<QuanTieComment> getPreviewRepliesForComments(
            @Param("commentIds") List<Long> commentIds,
            @Param("count") int count);
}
