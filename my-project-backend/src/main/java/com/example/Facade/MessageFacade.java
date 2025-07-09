package com.example.Facade;

import com.example.entity.dto.MessageUserSettings;
import com.example.entity.dto.Messages;
import com.example.enums.MessageEnums;
import com.example.service.MessageUserSettingsService;
import com.example.service.MessagesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 11:13
 */
@Service
public class MessageFacade {
    @Resource
    MessagesService messagesService;
    @Resource
    MessageUserSettingsService messageUserSettingsService;



}
