package com.example.entity.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/14 10:44
 */
@Data
public class ChatCreateMessageResp {
    //会话ID
    Long conversationId;
    //消息ID
    Long chatMessageId;
    //消息接受人名称（为前端的已读未读服务）
    List<String> readUserNames;
}
