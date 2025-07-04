package com.example.entity.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/4 11:11
 */
@Data
public class UserSearchResp {
    Long id;
    String username;
    String avatarUrl;
    Integer sex;
}
