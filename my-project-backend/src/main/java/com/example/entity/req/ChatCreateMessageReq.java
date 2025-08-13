package com.example.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 14:35
 */
@Data
public class ChatCreateMessageReq {
    @NotBlank(message = "消息不能为空")
    String content;
    @NotNull(message = "发送人ID不能为空")
    Long receiverId;
    Long chatConversationId;
    @NotNull(message = "聊天类型不能为空")
    Integer chatType;
    //@NotNull(message = "项目ID不能为空")
    Long projectId;

    Long currentUserId;
}
