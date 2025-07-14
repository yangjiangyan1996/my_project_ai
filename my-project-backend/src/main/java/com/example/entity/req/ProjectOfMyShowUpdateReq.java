package com.example.entity.req;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 10:25
 */
@Data
public class ProjectOfMyShowUpdateReq {
    private String audience;
    private String resources;
    private List<Long> skills;
    private Integer status;
    private String time;
}
