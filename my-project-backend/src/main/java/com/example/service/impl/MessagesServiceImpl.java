package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.MessageUserSettings;
import com.example.entity.dto.Messages;
import com.example.entity.dto.Projects;
import com.example.entity.resp.MsgOfCommentListResp;
import com.example.enums.MessageEnums;
import com.example.mapper.MessagesMapper;
import com.example.service.MessagesService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 11:21
 */
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages> implements MessagesService {



    @Override
    public Boolean createNotification(Long receiverId, Long senderId, Integer type, String content, Long relatedId, String relatedWords) {

        Messages e = new Messages();
        e.setSenderId(senderId);
        e.setReceiverId(receiverId);
        e.setType(type);
        e.setContent(content);
        e.setRelatedId(relatedId);
        e.setRelatedWords(relatedWords);
        e.setIsRead(MessageEnums.MessageReadStatus.UNREAD.getCode());
        e.setCreatedAt(new Date());
        e.setModifiedAt(new Date());
        e.setCreatedBy(senderId);
        e.setModifiedBy(senderId);
        return save(e);
    }

    @Override
    public Page<Messages> getPage(Page<Messages> pageable, Long userId, List<Integer> types) {
        return baseMapper.selectPage(
                pageable,
                new QueryWrapper<Messages>()
                        .eq("receiver_id", userId)
                        .in("type", types)
                        .orderByDesc("created_at")
        );
    }
}
