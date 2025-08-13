package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ChatMessageStatus;
import com.example.mapper.ChatMessageStatusMapper;
import com.example.service.ChatMessageStatusService;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 10:40
 */
@Service
public class ChatMessageStatusServiceImpl extends ServiceImpl<ChatMessageStatusMapper, ChatMessageStatus> implements ChatMessageStatusService {
}
