package com.example.entity.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/29 13:53
 */
@Data
public class UserAllInfo {
    Long id;
    String username;
    String nickname;
    String avatarUrl;
    Long industryCode;
    Integer sex;
    String email;
    String phone;
    String role;
    Date registerTime;
    String province;
    String city;
    String county;
}
