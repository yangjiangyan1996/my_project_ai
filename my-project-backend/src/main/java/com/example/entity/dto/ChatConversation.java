package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("chat_conversation")
public class ChatConversation extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer type;
    private String title;
    private String avatar;
    private Long lastMessageId;
    private java.util.Date lastActiveAt;
}