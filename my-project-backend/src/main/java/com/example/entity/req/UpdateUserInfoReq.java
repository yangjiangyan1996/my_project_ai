package com.example.entity.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/29 14:31
 */
@Data
public class UpdateUserInfoReq {
    Long id;
    String username;
    String nickname;
    Integer sex;
    String avatarUrl;
    String phone;
    String province;
    String city;
    String county;
}
