package com.example.entity.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/21 16:40
 */
@Data
public class UserCommentProjectReq {
    private String content;
    private Long projectId;
    private Long replyTo;
}
