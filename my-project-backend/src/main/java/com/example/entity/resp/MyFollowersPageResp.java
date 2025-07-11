package com.example.entity.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/10 23:15
 */
@Data
public class MyFollowersPageResp {
    Long id;
    Long userId;
    String username;
    String industryName;
    String avatarUrl;
    Long secrecyId;
    Boolean needFollow;
    Date createdAt;
}
