package com.example.entity.resp;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ProjectCommentResp {
    private Long id;                 // 评论ID
    private Long projectId;          // 所属副业项目ID
    private Long secrecyId;         // 评论用户保密id
    private String username;         // 评论用户昵称
    private String avatar;           // 用户头像（可选）
    private String content;          // 评论内容
    private Boolean deleted;         // 是否已删除
    private Boolean isMine;          // 是否为当前用户评论
    private Integer likes;           // 点赞数
    private Long replyTo;            // 回复的评论ID（如为 null 表示主评论）
    private String replyToName;      // 被回复用户昵称（仅回复评论时有值）
    private Long firstLevelCommonId;
    private Date createdAt;          // 评论时间
    private List<ProjectCommentResp> replies; // 子评论列表
}
