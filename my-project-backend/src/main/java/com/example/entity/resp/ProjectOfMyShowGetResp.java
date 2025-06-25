package com.example.entity.resp;

import lombok.Builder;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 10:25
 */
@Data
@Builder
public class ProjectOfMyShowGetResp {
    private Long id;
    private String audience;
    private String resources;
    private String skills;
    private Integer status;
    private String time;
}
