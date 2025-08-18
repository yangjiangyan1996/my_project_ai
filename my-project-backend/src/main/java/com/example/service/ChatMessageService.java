package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ChatMessage;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:40
 */
public interface ChatMessageService extends IService<ChatMessage> {
    Page<ChatMessage> selectPageByConversationId(Page<ChatMessage> page, Long conversationId);

    List<ChatMessage> selectListByConversationIdAndIdGreaterThan(Long chatConversationId, Long lastMessageId);

    List<ChatMessage> selectListByConversationId(Long conversationId);

    List<ChatMessage> selectLastMessagesOfConversations(List<Long> conversationIds);
}
