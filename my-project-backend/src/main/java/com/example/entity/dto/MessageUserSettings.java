package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("message_user_settings")
public class MessageUserSettings extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer receiveLikeNotification;
    private Integer receiveCommentNotification;
    private Integer receiveProjectNotification;
    private Integer receiveSystemNotification;
}