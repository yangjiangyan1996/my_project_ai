package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.TaskDefinition;
import com.example.mapper.TaskDefinitionMapper;
import com.example.service.TaskDefinitionService;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/5 11:59
 */
@Mapper
public class TaskDefinitionServiceImpl extends ServiceImpl<TaskDefinitionMapper, TaskDefinition> implements TaskDefinitionService {
}
