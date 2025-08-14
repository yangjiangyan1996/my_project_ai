package com.example.entity.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 20:38
 */
@Data
public class ChatNewMessagesReq {
    Long chatConversationId;
    Long lastMessageId;
    Long currentUserId;
}
