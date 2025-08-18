package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/18 10:02
 */
//
@Data
@TableName("project_review")
public class ProjectReview extends BaseModel{
    @TableId(type = IdType.AUTO)
    Long id;
    Long projectId;
    Long fromUserId;
    Long toUserId;
    Integer role;
    Integer score;
    String comment;
}
