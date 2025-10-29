package com.example.entity.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/4 11:09
 */
@Data
public class SearchUserReq {
    private String username;
    private Long tenantId;
}
