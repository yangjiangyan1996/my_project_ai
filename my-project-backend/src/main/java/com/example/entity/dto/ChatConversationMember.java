package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("chat_conversation_member")
public class ChatConversationMember extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private Long userId;
    private Integer role;
    private Long lastReadMessageId;
    private java.util.Date joinedAt;
    private java.util.Date quitAt;
}