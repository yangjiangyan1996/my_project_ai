package com.example.entity.resp;

import lombok.Data;
import java.util.List;

/**
 * @Author YangJian
 * @Description 检查消息已读状态响应
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 14:35
 */
@Data
public class ChatCheckReadStatusResp {
    private List<ReadStatus> readStatus;
    
    @Data
    public static class ReadStatus {
        private Long messageId;
        private List<String> unreadUsers;
    }
}