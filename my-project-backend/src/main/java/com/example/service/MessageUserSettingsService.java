package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.MessageUserSettings;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 11:12
 */
public interface MessageUserSettingsService extends IService<MessageUserSettings> {
    boolean saveMessageSetting(Long userId, Integer receiveLikeNotification,
                               Integer receiveCommentNotification,
                               Integer receiveProjectNotification,
                               Integer receiveSystemNotification);
}
