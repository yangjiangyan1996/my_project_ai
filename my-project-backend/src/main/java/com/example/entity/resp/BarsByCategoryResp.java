package com.example.entity.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/24 17:30
 */
@Data
public class BarsByCategoryResp {
    private Long id;
    private String name;
    private String avatar;
    private Integer followerCount;
    private Integer postCount;
}
