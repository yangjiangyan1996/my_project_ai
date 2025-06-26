package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.ProjectMembers;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMembersMapper extends BaseMapper<ProjectMembers> {
}