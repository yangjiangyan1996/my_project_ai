package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Messages;
import com.example.entity.resp.MsgOfCommentListResp;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 11:21
 */
public interface MessagesService extends IService<Messages> {
    Boolean createNotification(Long receiverId, Long senderId, Integer type, String content, Long relatedId, String relatedWords);

    Page<Messages> getProjectShowList(Page<Messages> page, Long userId);
}