package com.example.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description 检查消息已读状态请求
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 14:35
 */
@Data
public class ChatCheckReadStatusReq {
    @NotNull(message = "会话ID不能为空")
    Long conversationId;
    
    @NotNull(message = "用户ID不能为空")
    Long userId;
}