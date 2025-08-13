package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.AiRecommendLogs;
import com.example.entity.dto.ChatConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:36
 */
@Mapper
public interface ChatConversationMapper  extends BaseMapper<ChatConversation> {}

