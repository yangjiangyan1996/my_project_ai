package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.PartnerLocations;
import com.example.entity.dto.PostComments;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:35
 */
@Mapper

public interface PostCommentsMapper extends BaseMapper<PostComments> {}