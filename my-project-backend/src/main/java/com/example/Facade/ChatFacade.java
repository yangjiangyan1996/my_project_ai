package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.Account;
import com.example.entity.dto.ChatConversationMember;
import com.example.entity.dto.ChatMessage;
import com.example.entity.req.ChatHistoryPageReq;
import com.example.entity.resp.AchievementMyPageResp;
import com.example.entity.resp.ChatHistoryResp;
import com.example.mapper.ChatConversationMemberMapper;
import com.example.service.*;
import io.lettuce.core.internal.LettuceLists;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:48
 */
@Service
public class ChatFacade {

    @Resource
    private ChatConversationMemberService chatConversationMemberService;
    @Resource
    private ChatMessageService chatMessageService;
    @Resource
    private ChatConversationService chatConversationService;
    @Resource
    private AccountService accountService;
    @Resource
    private ChatMessageStatusService chatMessageStatusService;

    public Page<ChatHistoryResp> getChatHistory(ChatHistoryPageReq req) {
        List<ChatConversationMember> targetUserChatList = chatConversationMemberService.selectByUserId(req.getTargetUserId());
        List<ChatConversationMember> currentUserChatList = chatConversationMemberService.selectByUserId(req.getCurrentUserId());
        if (CollectionUtils.isEmpty(targetUserChatList) || CollectionUtils.isEmpty(currentUserChatList)) {
            return Page.of(req.getPage(), req.getSize());
        }

        //获取 targetUserChatList 和 currentUserChatList 的交集
        ChatConversationMember ccm = targetUserChatList.stream()
                .filter(currentUserChatList::contains)
                .collect(Collectors.toList()).stream().findFirst().orElse(null);
        if (ccm == null) {
            return Page.of(req.getPage(), req.getSize());
        }

        List<Account> accounts = accountService.selectByIds(LettuceLists.newList(req.getCurrentUserId(), req.getTargetUserId()));
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        Page<ChatMessage> chatMessagePage = chatMessageService.selectPageByConversationId(Page.of(req.getPage(), req.getSize()), ccm.getConversationId());
        List<ChatHistoryResp> list = chatMessagePage.getRecords().stream().map(v -> {
            ChatHistoryResp p = new ChatHistoryResp();
            p.setChatMessageId(v.getId());
            p.setChatConversationId(ccm.getConversationId());
            p.setContent(v.getContent());
            p.setCreatedTime(v.getCreatedAt());
            p.setMessageType(v.getMessageType());
            p.setStatus(v.getStatus());
            p.setSenderId(v.getSenderId());
            p.setSenderAvatar(userId2UserInfoMap.getOrDefault(v.getSenderId(), new Account()).getAvatarUrl());
            p.setSenderName(userId2UserInfoMap.getOrDefault(v.getSenderId(), new Account()).getNickname());
            p.setIsSelf(v.getSenderId().equals(req.getCurrentUserId()));
            return p;
        }).collect(Collectors.toList());

        Page<ChatHistoryResp> result = Page.of(req.getPage(), req.getSize());
        result.setTotal(chatMessagePage.getTotal());
        result.setRecords(list);
        return result;
    }
}
