package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("quan_tie_comments")
public class QuanTieComment extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tieId;
    private Long userId;
    private Long firstLevelCommonId;
    private String username;
    private String content;
    private Long replyTo;
    private Integer status;
    private Long replyToUserId;
    private String replyToUsername;
    private Integer likes;

    @TableField(exist = false)
    private Integer count;
}