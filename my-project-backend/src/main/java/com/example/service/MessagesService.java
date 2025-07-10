package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Messages;
import com.example.entity.resp.MsgOfCommentListResp;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 11:21
 */
public interface MessagesService extends IService<Messages> {
    Boolean createNotification(Long receiverId, Long senderId, Integer type, String content, Long relatedId, String relatedWords);

    Page<Messages> getPage(Page<Messages> page, Long userId, List<Integer> types);

    Long unreadMsgCount(Long userId,List<Integer> types);

    Boolean markMsgAsRead(Long id, Long userId);

    Integer allMarkRead(List<Integer> types, Long userId);
}