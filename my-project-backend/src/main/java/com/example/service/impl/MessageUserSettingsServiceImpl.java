package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.MessageUserSettings;
import com.example.mapper.MessageUserSettingsMapper;
import com.example.service.MessageUserSettingsService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 11:12
 */
@Service
public class MessageUserSettingsServiceImpl extends ServiceImpl<MessageUserSettingsMapper, MessageUserSettings> implements MessageUserSettingsService {
    @Override
    public boolean saveMessageSetting(Long userId, Integer receiveLikeNotification,
                                      Integer receiveCommentNotification,
                                      Integer receiveProjectNotification,
                                      Integer receiveSystemNotification) {
        MessageUserSettings e = new MessageUserSettings();
        e.setUserId(userId);
        e.setReceiveCommentNotification(receiveCommentNotification);
        e.setReceiveLikeNotification(receiveLikeNotification);
        e.setReceiveProjectNotification(receiveProjectNotification);
        e.setReceiveSystemNotification(receiveSystemNotification);
        e.setCreatedAt(new Date());
        e.setModifiedAt(new Date());
        e.setCreatedBy(userId);
        e.setModifiedBy(userId);
        return save(e);
    }
}
