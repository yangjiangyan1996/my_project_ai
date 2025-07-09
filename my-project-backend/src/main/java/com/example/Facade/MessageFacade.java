package com.example.Facade;

import com.example.entity.dto.MessageUserSettings;
import com.example.entity.dto.Messages;
import com.example.entity.dto.ProjectComment;
import com.example.entity.dto.Projects;
import com.example.enums.MessageEnums;
import com.example.service.MessageUserSettingsService;
import com.example.service.MessagesService;
import com.example.service.ProjectCommentService;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 11:13
 */
@Service
@Slf4j
public class MessageFacade {
    @Resource
    MessagesService messagesService;
    @Resource
    MessageUserSettingsService messageUserSettingsService;
    @Resource
    ProjectCommentService projectCommentService;
    @Resource
    ProjectService projectService;


    public void createMessageOfComment(Long projectId, Long replyTo, Long currentUserId) {
        Long receiverId = null; // 获取接收者id
        String content = null;
        Integer type = null;
        Long relatedId = null;
        String relatedWords = null;
        Projects p = projectService.selectByProjectId(projectId);
        if (replyTo != null) {
            ProjectComment pc = projectCommentService.selectById(replyTo);
            if (pc != null) {
                receiverId = pc.getUserId();
                content = "回复了您的评论";
                type = MessageEnums.MessageType.REPLY_COMMENT.getCode();
                relatedId = replyTo;
                relatedWords = pc.getContent();
            }
        } else if(projectId != null) {
            receiverId = p.getCreatedBy();
            content = "评论了您的项目";
            type = MessageEnums.MessageType.REPLY_PROJECT.getCode();
            relatedId = projectId;
            relatedWords = p.getName();
        }

        if (Objects.equals(receiverId, currentUserId)) {
            log.info("MessageFacade#createMessageOfComment，消息接收者与发送者相同，不发送消息,id:{}",currentUserId);
            return;
        }
        messagesService.createNotification(receiverId, currentUserId, type, content, relatedId,relatedWords);
    }
}
