package com.example.entity.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/1 17:15
 */
@Data
public class ProjectUpdateResp {
    private Long id;
    private String name;
    private Integer category;
    private String description;
    private Integer difficulty;
    private String imageUrl;
    private Integer status;
    private String reason;
    private String steps;
    private String tools;
    private String timePerDay;
    private String incomeEstimate;
    private Long targetAudience;
    private String targetAudienceName;
    private String riskWarning;
    private Integer isRemote;
    private Integer isFreeEntry;
    private String tags;
    private Integer needMember;
    private Integer memberNum;
}
