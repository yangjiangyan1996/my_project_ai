package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ChatConversationMember;
import com.example.mapper.ChatConversationMemberMapper;
import com.example.service.ChatConversationMemberService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:39
 */
@Service
public class ChatConversationMemberServiceImpl extends ServiceImpl<ChatConversationMemberMapper, ChatConversationMember> implements ChatConversationMemberService {
    @Override
    public Page<ChatConversationMember> selectPageByUserId(Page<ChatConversationMember> of, Long currentUserId) {
        if (currentUserId == null) {
            return Page.of(of.getCurrent(), of.getSize());
        }
        return this.baseMapper.selectPage(of, new QueryWrapper<ChatConversationMember>()
                .eq("user_id", currentUserId)
                .eq("is_deleted", 0)
                .orderByDesc("last_read_message_id")
        );
    }

    @Override
    public ChatConversationMember selectByConversationIdAndUserId(Long conversationId, Long currentUserId) {
        if (conversationId == null || currentUserId == null) {
            return null;
        }
        return this.baseMapper.selectOne(new QueryWrapper<ChatConversationMember>()
                .eq("conversation_id", conversationId)
                .eq("user_id", currentUserId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<ChatConversationMember> selectByConversationIds(List<Long> conversationIds) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return new ArrayList<>();
        }
        return this.baseMapper.selectList(new QueryWrapper<ChatConversationMember>()
                .in("conversation_id", conversationIds)
                .eq("is_deleted", 0));
    }

    @Override
    public List<ChatConversationMember> selectByConversationId(Long conversationId) {
        if (conversationId == null) {
            return new ArrayList<>();
        }
        return this.baseMapper.selectList(
                new QueryWrapper<ChatConversationMember>()
                        .eq("conversation_id", conversationId)
                        .eq("is_deleted", 0));
    }

    @Override
    public List<ChatConversationMember> selectByUserId(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return this.baseMapper.selectList(new QueryWrapper<ChatConversationMember>()
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }
}
