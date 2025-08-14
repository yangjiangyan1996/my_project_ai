package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("chat_message_status")
public class ChatMessageStatus extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private Long messageId;
    private Long userId;
    private Integer isRead;
    private java.util.Date readAt;
}