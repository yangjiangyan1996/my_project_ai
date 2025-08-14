package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ChatMessage;
import com.example.mapper.ChatMessageMapper;
import com.example.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:40
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Override
    public ChatMessageMapper getBaseMapper() {
        return super.getBaseMapper();
    }
    @Override
    public Page<ChatMessage> selectPageByConversationId(Page<ChatMessage> page, Long conversationId) {
        return page(page, new QueryWrapper<ChatMessage>()
                .eq("conversation_id", conversationId)
                .eq("is_deleted", 0)
                .orderByDesc("created_at"));
    }

    @Override
    public List<ChatMessage> selectListByConversationIdAndIdGreaterThan(Long chatConversationId, Long lastMessageId) {
        return list(new QueryWrapper<ChatMessage>()
                .eq("conversation_id", chatConversationId)
                .gt("id", lastMessageId)
                .eq("is_deleted", 0)
                .orderByDesc("created_at"));
    }

    @Override
    public List<ChatMessage> selectListByConversationId(Long conversationId) {
        return list(new QueryWrapper<ChatMessage>()
                .eq("conversation_id", conversationId)
                .eq("is_deleted", 0)
                .orderByDesc("created_at"));
    }


}
