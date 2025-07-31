package com.example.entity.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/24 17:30
 */
@Data
public class BarsInfoResp {
    private Long id;
    private String name;
    private String avatar;
    private Long followerCount;
    private Long postCount;
    private String description;
    private String firstCategoryName;
    private String secondCategoryName;
    private Boolean followed;
    private String createdName;
    private String createdTime;
    private String createdAvatar;
    private Long secrecyId;

}
