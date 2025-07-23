package com.example.entity.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/15 17:29
 */
@Data
public class MatchUserResp {
    private Long userId;
    private Long secrecyId;
    private String username;
    private String avatarUrl;
    private Integer sex;
    private Integer statusOfUserInProject;
    private Integer matchScore;
    String province;
    String city;
    String county;

    private String audienceName;
    private String resources;
    private String skillNames;
    private String timePerDay;
}
