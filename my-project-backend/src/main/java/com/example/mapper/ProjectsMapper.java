package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.ProjectRankings;
import com.example.entity.dto.Projects;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:55
 */
@Mapper

public interface ProjectsMapper extends BaseMapper<Projects> {}
