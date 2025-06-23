package com.example.entity.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 23:48
 */
@Data
public class MyPublishedResp {
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private java.util.Date createdAt;
    private Long likeCount;
    private Long commentCount;
    private Long favoriteCount;
}
