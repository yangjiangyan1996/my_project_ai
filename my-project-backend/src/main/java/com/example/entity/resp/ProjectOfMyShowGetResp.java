package com.example.entity.resp;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 10:25
 */
@Data
@NoArgsConstructor
public class ProjectOfMyShowGetResp {
    private Long id;
    private Long userId;
    private Long secrecyId;
    private String userName;
    private Long audience;
    private String audienceName;
    private String resources;
    private String skills;
    private String skillNames;
    private Integer status;
    private Long timePerDay;
}
