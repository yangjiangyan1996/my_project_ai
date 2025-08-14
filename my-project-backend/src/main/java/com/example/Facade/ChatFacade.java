package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.AsyncTaskUtil;
import com.example.entity.dto.*;
import com.example.entity.req.ChatCreateMessageReq;
import com.example.entity.req.ChatHistoryPageReq;
import com.example.entity.req.ChatNewMessagesReq;
import com.example.entity.req.ChatCheckReadStatusReq;
import com.example.entity.resp.ChatCreateMessageResp;
import com.example.entity.resp.ChatHistoryResp;
import com.example.entity.resp.ChatCheckReadStatusResp;
import com.example.enums.ChatEnums;
import com.example.service.*;
import io.lettuce.core.internal.LettuceLists;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:48
 */
@Service
@Slf4j
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

        //获取消息是否已读
        List<Long> messageIds = chatMessagePage.getRecords().stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
        List<ChatMessageStatus> cmsList = chatMessageStatusService.selectByMessageIds(messageIds, ccm.getConversationId(), req.getCurrentUserId());
        Map<Long, List<ChatMessageStatus>> messageId2CmsMap = cmsList.stream().collect(Collectors.groupingBy(v -> v.getMessageId()));

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
            if (messageId2CmsMap.containsKey(v.getId())) {
                List<ChatMessageStatus> cmsOfMessageList = messageId2CmsMap.get(v.getId());
                List<String> unReadUserNikeName = cmsOfMessageList.stream()
                        .filter(cms -> cms.getCreatedBy().equals(req.getCurrentUserId()))
                        .filter(cms -> ChatEnums.IsReadEnum.UNREAD.getCode().equals(cms.getIsRead()))
                        .map(cms -> userId2UserInfoMap.get(cms.getUserId()).getNickname())
                        .collect(Collectors.toList());
                p.setReadUserNames(unReadUserNikeName);
            }
            return p;
        }).sorted(Comparator.comparing(ChatHistoryResp::getCreatedTime)).collect(Collectors.toList());


        //这里记录用户的已读
        AsyncTaskUtil.execute(() -> {
            try{
                //获取list中最后一个ID
                Long lastMessageId = list.get(list.size() - 1).getChatMessageId();
                updateUserRead(ccm.getConversationId(), req.getCurrentUserId(), lastMessageId);
            }catch (Exception e) {
                log.error("ChatFacade#getChatHistory, 更新用户已读失败",e);
            }
        });

        Page<ChatHistoryResp> result = Page.of(req.getPage(), req.getSize());
        result.setTotal(chatMessagePage.getTotal());
        result.setRecords(list);
        return result;
    }


    /**
     * 更新用户已读
     *
     * @param conversationId 会话id
     * @param currentUserId  当前用户id
     * @param lastMessageId  最后一条消息id
     */
    private void updateUserRead(Long conversationId, Long currentUserId, Long lastMessageId) {
        ChatConversationMember ccm = chatConversationMemberService.selectByConversationIdAndUserId(conversationId, currentUserId);
        if (ccm == null) {
            log.warn("ChatFacade#updateUserRead, 用户未加入该会话，无法更新已读消息,conversationId;{} currentUserId:{}", conversationId, currentUserId);
            return;
        }
        if (ccm.getLastReadMessageId() != null && ccm.getLastReadMessageId() > lastMessageId) {
            log.warn("ChatFacade#updateUserRead, 已读消息已更新，无需再次更新,conversationId;{} currentUserId:{}", conversationId, currentUserId);
        } else {
            ccm.setLastReadMessageId(lastMessageId);
            chatConversationMemberService.updateById(ccm);
            chatMessageStatusService.updateReadStatuByConversationIdAndUserId(conversationId, currentUserId, ChatEnums.IsReadEnum.UNREAD.getCode(), lastMessageId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ChatCreateMessageResp sendMessage(ChatCreateMessageReq req) {
        if (req.getChatConversationId() == null) {
            //第一次发起聊天，创建会话内容
            Long chatConversationId = initChatConversation(req);
            req.setChatConversationId(chatConversationId);
        }
        if (req.getChatConversationId() == null) {
            throw new ValidationException("聊天会话ID不能为空");
        }

        ChatMessage cm = new ChatMessage();
        cm.setConversationId(req.getChatConversationId());
        cm.setSenderId(req.getCurrentUserId());
        cm.setMessageType(ChatEnums.MessageTypeEnum.TEXT.getCode());
        cm.setContent(req.getContent());
        cm.setStatus(ChatEnums.StatusEnum.NORMAL.getCode());
        boolean save = chatMessageService.save(cm);
        if (save) {
            AsyncTaskUtil.execute(() -> {
                try{
                    createMessageStatus(req.getChatConversationId(), cm.getId(), req.getCurrentUserId());
                }catch (Exception e) {
                    log.error("Chat#sendMessage,创建消息状态失败, ConversationId:{},messageId:{},userId:{}", req.getChatConversationId(), cm.getId(), req.getCurrentUserId(), e);
                }

                try{
                    chatConversationService.updateLastActiveAtById(new Date(), req.getChatConversationId());
                }catch (Exception e){
                    log.error("Chat#sendMessage,更新会话最后活跃时间失败, ConversationId:{}", req.getChatConversationId(), e);
                }
            });
        }
        ChatCreateMessageResp r = new ChatCreateMessageResp();
        r.setChatMessageId(cm.getId());
        r.setConversationId(req.getChatConversationId());
        return r;
    }

    private Boolean createMessageStatus(Long conversationId, Long messageId, Long createById) {
        List<ChatConversationMember> list = chatConversationMemberService.selectByConversationId(conversationId);
        List<ChatMessageStatus> batchSaveList = new ArrayList<>();
        for (ChatConversationMember member : list) {
            ChatMessageStatus status = new ChatMessageStatus();
            status.setConversationId(conversationId);
            status.setMessageId(messageId);
            status.setUserId(member.getUserId());
            if (member.getUserId().equals(createById)) {
                status.setIsRead(ChatEnums.IsReadEnum.READ.getCode());
            } else {
                status.setIsRead(ChatEnums.IsReadEnum.UNREAD.getCode());
            }
            status.setIsDeleted(0);
            status.setCreatedBy(createById);
            status.setCreatedAt(new Date());
            status.setModifiedBy(createById);
            status.setModifiedAt(new Date());
            batchSaveList.add(status);
        }
        chatMessageStatusService.saveBatch(batchSaveList);
        return true;
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

    public ChatCheckReadStatusResp checkReadStatus(ChatCheckReadStatusReq req) {
        // 获取会话中的所有消息
        List<ChatMessage> messages = chatMessageService.selectListByConversationId(req.getConversationId());
        if (CollectionUtils.isEmpty(messages)) {
            return new ChatCheckReadStatusResp();
        }

        // 获取消息ID列表
        List<Long> messageIds = messages.stream().map(ChatMessage::getId).collect(Collectors.toList());
        // 查询消息状态
        List<ChatMessageStatus> cmsList = chatMessageStatusService.selectByMessageIds(messageIds, req.getConversationId(), req.getUserId());

        // 获取用户信息
        List<Long> userIds = cmsList.stream().map(ChatMessageStatus::getUserId).distinct().collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(Account::getId, account -> account));

        // 构造响应
        ChatCheckReadStatusResp resp = new ChatCheckReadStatusResp();
        List<ChatCheckReadStatusResp.ReadStatus> readStatusList = new ArrayList<>();

        // 按消息ID分组
        Map<Long, List<ChatMessageStatus>> messageId2StatusMap = cmsList.stream()
                .collect(Collectors.groupingBy(ChatMessageStatus::getMessageId));

        // 遍历每条消息，构造已读状态
        for (ChatMessage message : messages) {
            ChatCheckReadStatusResp.ReadStatus readStatus = new ChatCheckReadStatusResp.ReadStatus();
            readStatus.setMessageId(message.getId());

            // 获取该消息的所有状态
            List<ChatMessageStatus> statuses = messageId2StatusMap.getOrDefault(message.getId(), Collections.emptyList());

            // 找出未读的用户
            List<String> unreadUsers = statuses.stream()
                    .filter(status -> ChatEnums.IsReadEnum.UNREAD.getCode().equals(status.getIsRead()))
                    .map(status -> userId2UserInfoMap.get(status.getUserId()).getNickname())
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(unreadUsers)) {
                continue;
            }
            readStatus.setUnreadUsers(unreadUsers);
            readStatusList.add(readStatus);
        }

        resp.setReadStatus(readStatusList);
        return resp;
    }

    public List<ChatHistoryResp> getNewMessages(ChatNewMessagesReq req) {
        List<ChatMessage> list = chatMessageService.selectListByConversationIdAndIdGreaterThan(req.getChatConversationId(), req.getLastMessageId());
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<Long> userIds = list.stream().map(v -> v.getSenderId()).distinct().collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        //获取消息是否已读
        List<Long> messageIds = list.stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
        List<ChatMessageStatus> cmsList = chatMessageStatusService.selectByMessageIds(messageIds, req.getChatConversationId(), req.getCurrentUserId());
        Map<Long, List<ChatMessageStatus>> messageId2CmsMap = cmsList.stream().collect(Collectors.groupingBy(v -> v.getMessageId()));


        List<ChatHistoryResp> result = list.stream().map(v -> {
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

            if (messageId2CmsMap.containsKey(v.getId())) {
                List<ChatMessageStatus> cmsOfMessageList = messageId2CmsMap.get(v.getId());
                List<String> unReadUserNikeName = cmsOfMessageList.stream()
                        .filter(cms -> cms.getCreatedBy().equals(req.getCurrentUserId()))
                        .filter(cms -> ChatEnums.IsReadEnum.UNREAD.getCode().equals(cms.getIsRead()))
                        .map(cms -> userId2UserInfoMap.get(cms.getUserId()).getNickname())
                        .collect(Collectors.toList());
                p.setReadUserNames(unReadUserNikeName);
            }
            return p;
        }).sorted(Comparator.comparing(ChatHistoryResp::getCreatedTime)).collect(Collectors.toList());

        AsyncTaskUtil.execute(() -> {
            try{
                //获取list中最后一个ID
                Long lastMessageId = result.get(list.size() - 1).getChatMessageId();
                updateUserRead(req.getChatConversationId(), req.getCurrentUserId(), lastMessageId);
            }catch (Exception e) {
                log.error("ChatFacade#getNewMessages, 更新用户已读失败",e);
            }
        });

        return result;
    }
}
