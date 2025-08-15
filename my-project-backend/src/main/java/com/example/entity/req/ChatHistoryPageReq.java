package com.example.entity.req;

import com.example.entity.base.PageReq;
import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 11:58
 */
@Data
public class ChatHistoryPageReq extends PageReq {
    //前端传(访问他人主页发起私聊)
    private Long secrecyId;
    //后端填充(访问他人主页发起私聊)
    private Long targetUserId;

    //前端传（主页私聊通知）
    private Long chatId;

    private Integer chatType;

    //后端填充
    private Long currentUserId;
}
