package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.QuanPosts;
import com.example.entity.dto.QuanTieComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:25
 */
@Mapper
public interface QuanTieCommentMapper extends BaseMapper<QuanTieComment> {}
