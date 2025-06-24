package com.example.entity.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 23:48
 */
@Data
@AllArgsConstructor
public class MyFollowCountResp {
    private Integer followerCount;
    private Integer followeeCount;
}
