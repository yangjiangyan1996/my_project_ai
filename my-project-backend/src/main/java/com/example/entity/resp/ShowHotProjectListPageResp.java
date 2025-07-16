package com.example.entity.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/16 10:27
 */
@Data
public class ShowHotProjectListPageResp {
    private Long projectId;
    private String name;
    private String imageUrl;
    private String description;
    private Date createdAt;
    private String status;
    String firstCategoryName ;
    String secondCategoryName;

    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Double score;
    private int rank;
}
