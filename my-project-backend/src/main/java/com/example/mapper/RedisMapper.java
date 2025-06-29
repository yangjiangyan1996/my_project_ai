package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.RedisDTO;
import com.example.entity.dto.UserFavorites;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/28 16:32
 */
@Mapper
public interface RedisMapper  extends BaseMapper<RedisDTO> {}
