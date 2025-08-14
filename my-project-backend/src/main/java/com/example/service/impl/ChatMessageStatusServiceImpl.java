package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ChatMessageStatus;
import com.example.enums.ChatEnums;
import com.example.mapper.ChatMessageStatusMapper;
import com.example.service.ChatMessageStatusService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:40
 */
@Service
public class ChatMessageStatusServiceImpl extends ServiceImpl<ChatMessageStatusMapper, ChatMessageStatus> implements ChatMessageStatusService {
    @Override
    public List<ChatMessageStatus> selectByMessageIds(List<Long> messageIds, Long conversationId,Long currentUserId) {
        if (messageIds == null || messageIds.isEmpty()||conversationId==null || currentUserId==null) {
            return new ArrayList<>();
        }
        return this.baseMapper.selectList(new QueryWrapper<ChatMessageStatus>()
                .in("message_id", messageIds)
                .eq("conversation_id", conversationId)
                .eq("created_by", currentUserId)
                .eq("is_deleted", 0));
    }

    @Override
    public Integer updateReadStatuByConversationIdAndUserId(Long conversationId, Long currentUserId, Integer code, Long lastMessageId) {
        ChatMessageStatus s = new ChatMessageStatus();
        s.setIsRead(ChatEnums.IsReadEnum.READ.getCode());
        return this.baseMapper.update(s, new QueryWrapper<ChatMessageStatus>()
                .eq("conversation_id", conversationId)
                .eq("user_id", currentUserId)
                .eq("is_read", code)
                //小于等于messageId
                .le("message_id", lastMessageId)
                .eq("is_deleted", 0));
    }
}
