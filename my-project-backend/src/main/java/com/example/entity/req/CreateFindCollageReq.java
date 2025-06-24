package com.example.entity.req;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/24 21:59
 */
@Data
public class CreateFindCollageReq {
    private String name;
    private Integer category;
    private String description;
    private Integer difficulty;
    //封面图片
    private String imageUrl;
    private Integer status;
    private String steps;
    private String tools;
    private String timePerDay;
    private String incomeEstimate;
    private String targetAudience;
    private String riskWarning;
    private Integer isRemote;
    private Integer isFreeEntry;
    private String tags;
}
