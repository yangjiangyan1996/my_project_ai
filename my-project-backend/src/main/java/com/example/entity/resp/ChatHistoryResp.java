package com.example.entity.resp;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 11:51
 */
@Data
public class ChatHistoryResp {
    private Long chatMessageId;
    private Long chatConversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Boolean isSelf;
    private String content;
    private Integer messageType;
    private Date createdTime;
    private Integer status;
    private List<String> readUserNames;
}
