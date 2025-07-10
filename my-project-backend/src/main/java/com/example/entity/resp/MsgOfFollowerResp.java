package com.example.entity.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 16:32
 */
@Data
public class MsgOfFollowerResp {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private Integer isRead;
    private Date createdAt;
    //是否需要回关
    private Boolean needFollow;
}
