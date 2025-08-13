package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ChatConversation;
import com.example.entity.dto.ChatMessage;
import com.example.mapper.ChatConversationMapper;
import com.example.service.ChatConversationService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:39
 */
@Service
public class ChatConversationServiceImpl extends ServiceImpl<ChatConversationMapper, ChatConversation> implements ChatConversationService {
    @Override
    public boolean updateLastActiveAtById(Date date, Long Id) {
        ChatConversation c = new ChatConversation();
        c.setLastActiveAt(date);
        return this.update(c, new QueryWrapper<ChatConversation>().eq("id", Id));
    }

    @Override
    public List<ChatConversation> selectByIds(List<Long> ids, Integer chatType) {
        return this.baseMapper.selectList(new QueryWrapper<ChatConversation>()
                .in("id", ids)
                .eq(chatType != null, "chat_type", chatType)
                .eq("is_deleted", 0));
    }
}
