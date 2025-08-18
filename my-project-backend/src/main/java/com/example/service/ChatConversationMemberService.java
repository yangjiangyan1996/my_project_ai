package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ChatConversationMember;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:39
 */
public interface ChatConversationMemberService extends IService<ChatConversationMember> {
    List<ChatConversationMember> selectByUserId(Long targetUserId);

    List<ChatConversationMember> selectByConversationId(Long conversationId);

    List<ChatConversationMember> selectByConversationIds(List<Long> conversationIds);

    ChatConversationMember selectByConversationIdAndUserId(Long conversationId, Long currentUserId);

    Page<ChatConversationMember> selectPageByUserId(Page<ChatConversationMember> of, Long currentUserId);
}
