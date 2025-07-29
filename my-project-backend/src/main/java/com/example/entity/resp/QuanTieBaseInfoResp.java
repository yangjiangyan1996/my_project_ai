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
public class QuanTieBaseInfoResp {
    private Long id;
    private String title;
    private List<String> avatar;
    private String content;
    private String createdName;
    private String createdTime;
    private Integer views;
    private Integer comments;
    private Integer likes;
}
