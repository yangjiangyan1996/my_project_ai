package com.example.entity.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/27 11:08
 */
@Data
public class MyApplyListResp {
    private Long id;
    private String projectName;
    private String description;

    private String avatarUrl;
    private String userName;
    private Long userId;
    private Long secrecyId;

    private Integer status;
    private Date applyTime;
    private String message;

    //技能
    private String timePerDayStr;
    private String audience;
    private String skillsName;
    private String resources;
}
