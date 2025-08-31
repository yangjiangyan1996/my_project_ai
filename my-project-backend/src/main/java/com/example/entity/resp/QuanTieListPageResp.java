package com.example.entity.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 15:25
 */
@Data
public class QuanTieListPageResp {
    private Long id;
    private String title;
    private String userAvatar;
    private List<String> avatar;
    private String createdName;
    private String createdTime;
    private Integer views;
    private Integer comments;
    private Integer likes;
    private String barName;
    Boolean liked;
}
