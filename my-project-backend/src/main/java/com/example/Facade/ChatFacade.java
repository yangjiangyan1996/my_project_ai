package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.AsyncTaskUtil;
import com.example.entity.dto.Account;
import com.example.entity.dto.ChatConversation;
import com.example.entity.dto.ChatConversationMember;
import com.example.entity.dto.ChatMessage;
import com.example.entity.req.ChatCreateMessageReq;
import com.example.entity.req.ChatHistoryPageReq;
import com.example.entity.req.ChatNewMessagesReq;
import com.example.entity.resp.ChatHistoryResp;
import com.example.enums.ChatEnums;
import com.example.service.*;
import io.lettuce.core.internal.LettuceLists;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        //获取currentUserChatList，targetUserChatList集合中的conversationId
        List<Long> conversationIds = Stream.concat(
                        targetUserChatList.stream(),
                        currentUserChatList.stream()
                )
                .map(ChatConversationMember::getConversationId)
                .collect(Collectors.toList());
        List<ChatConversation> chatMessages = chatConversationService.selectByIds(conversationIds, req.getChatType());
        List<Long> twoUserConversionIds = chatMessages.stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
        //获取 targetUserChatList 和 currentUserChatList 的交集
        List<Long> currentConversionIds = currentUserChatList.stream().map(v -> v.getConversationId()).distinct().collect(Collectors.toList());
        ChatConversationMember ccm = targetUserChatList.stream()
                .filter(c -> twoUserConversionIds.contains(c.getConversationId()))
                .filter(c -> currentConversionIds.contains(c.getConversationId()))
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
        }).sorted(Comparator.comparing(ChatHistoryResp::getCreatedTime)).collect(Collectors.toList());

        Page<ChatHistoryResp> result = Page.of(req.getPage(), req.getSize());
        result.setTotal(chatMessagePage.getTotal());
        result.setRecords(list);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(ChatCreateMessageReq req) {
        if (req.getChatConversationId() == null) {
            //第一次发起聊天，创建会话内容
            Long chatConversationId = initChatConversation(req);
            req.setChatConversationId(chatConversationId);
        }

        ChatMessage cm = new ChatMessage();
        cm.setConversationId(req.getChatConversationId());
        cm.setSenderId(req.getCurrentUserId());
        cm.setMessageType(ChatEnums.MessageTypeEnum.TEXT.getCode());
        cm.setContent(req.getContent());
        cm.setStatus(ChatEnums.StatusEnum.NORMAL.getCode());
        boolean save = chatMessageService.save(cm);
        if (save) {
            AsyncTaskUtil.execute(() -> chatConversationService.updateLastActiveAtById(new Date(), req.getChatConversationId()));
        }
        return req.getChatConversationId();
    }

    private Long initChatConversation(ChatCreateMessageReq req) {
        Long chatConversationId = null;
        if (req.getChatType().equals(ChatEnums.TypeEnum.SINGLE.getCode())) {
            List<ChatConversationMember> targetUserChatList = chatConversationMemberService.selectByUserId(req.getReceiverId());
            List<ChatConversationMember> currentUserChatList = chatConversationMemberService.selectByUserId(req.getCurrentUserId());

            //获取 targetUserChatList 和 currentUserChatList 的交集
            ChatConversationMember ccmOfTwo = targetUserChatList.stream()
                    .filter(currentUserChatList::contains)
                    .collect(Collectors.toList()).stream().findFirst().orElse(null);
            if (ccmOfTwo != null) {
                chatConversationId = ccmOfTwo.getConversationId();
            } else {
                ChatConversation cc = new ChatConversation();
                cc.setChatType(req.getChatType());
                cc.setProjectId(req.getProjectId());
                cc.setTitle(req.getChatType().equals(ChatEnums.TypeEnum.SINGLE.getCode()) ? "单聊" : "群聊");
                cc.setLastActiveAt(new Date());
                cc.setCreatedAt(new Date());
                cc.setCreatedBy(req.getCurrentUserId());
                cc.setModifiedAt(new Date());
                cc.setModifiedBy(req.getCurrentUserId());

                boolean ccSave = chatConversationService.save(cc);
                if (ccSave) {
                    //单聊
                    List<Long> userIds = LettuceLists.newList(req.getReceiverId(), req.getCurrentUserId());
                    List<ChatConversationMember> list = userIds.stream().map(v -> {
                        ChatConversationMember ccm = new ChatConversationMember();
                        ccm.setConversationId(cc.getId());
                        ccm.setUserId(v);
                        ccm.setJoinedAt(new Date());
                        return ccm;
                    }).collect(Collectors.toList());
                    boolean ccmSave = chatConversationMemberService.saveBatch(list);
                    if (!ccmSave) {
                        throw new ValidationException("创建聊天会话失败");
                    }
                    chatConversationId = cc.getId();
                }
            }
        } else if (req.getChatType().equals(ChatEnums.TypeEnum.GROUP.getCode())) {

        }

        return chatConversationId;
    }

    public List<ChatHistoryResp> getNewMessages(ChatNewMessagesReq req) {
        List<ChatMessage> list = chatMessageService.selectListByConversationIdAndIdGreaterThan(req.getChatConversationId(), req.getLastMessageId());

        List<Long> userIds = list.stream().map(v -> v.getSenderId()).distinct().collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));


        return list.stream().map(v -> {
            ChatHistoryResp p = new ChatHistoryResp();
            p.setChatMessageId(v.getId());
            p.setChatConversationId(req.getChatConversationId());
            p.setContent(v.getContent());
            p.setCreatedTime(v.getCreatedAt());
            p.setMessageType(v.getMessageType());
            p.setStatus(v.getStatus());
            p.setSenderId(v.getSenderId());
            p.setSenderAvatar(userId2UserInfoMap.getOrDefault(v.getSenderId(), new Account()).getAvatarUrl());
            p.setSenderName(userId2UserInfoMap.getOrDefault(v.getSenderId(), new Account()).getNickname());
            p.setIsSelf(v.getSenderId().equals(req.getCurrentUserId()));
            return p;
        }).sorted(Comparator.comparing(ChatHistoryResp::getCreatedTime)).collect(Collectors.toList());
    }
}
