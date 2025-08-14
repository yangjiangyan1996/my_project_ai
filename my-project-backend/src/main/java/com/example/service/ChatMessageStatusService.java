package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ChatMessage;
import com.example.entity.dto.ChatMessageStatus;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:40
 */
public interface ChatMessageStatusService extends IService<ChatMessageStatus> {
    Integer updateReadStatuByConversationIdAndUserId(Long conversationId, Long currentUserId, Integer code, Long lastMessageId);

    List<ChatMessageStatus> selectByMessageIds(List<Long> messageIds,Long conversationId, Long createBy);
}
