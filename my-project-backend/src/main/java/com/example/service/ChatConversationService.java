package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ChatConversation;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:39
 */
public interface ChatConversationService extends IService<ChatConversation> {
    boolean updateLastActiveAtById(Date date, Long Id);

    List<ChatConversation> selectByIds(List<Long> ids, Integer chatType);

    ChatConversation selectByProjectId(Long projectId);
}
