package com.example.entity.resp;
import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/14 17:44
 */

@Data
public class ChatListResp {
    private Long chatId;
    private Integer chatType;
    private String title;
    private String avatar;
    private String lastMessage;
    //最后活跃时间
    private Date lastActiveAt;
    private Long unreadCount;
}
