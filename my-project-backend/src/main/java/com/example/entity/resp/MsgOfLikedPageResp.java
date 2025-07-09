package com.example.entity.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 19:24
 */
@Data
public class MsgOfLikedPageResp {
    private Long id;
    private String senderName;
    private String content;
    private Integer isRead;
    private String relatedWords;
    private Date createdAt;
}
