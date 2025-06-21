package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("project_comments")
public class ProjectComment extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long userId;
    private String username;
    private String content;
    private Long replyTo;
    private Integer status;
    private Long replyToUserId;
    private String replyToUsername;
    private Integer likes;
}