package com.example.entity.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/28 22:35
 */
@Data
public class QuanTieTodayHotResp {
    private Long id;
    private String title;
    private Integer views;
    private Integer comments;
}
