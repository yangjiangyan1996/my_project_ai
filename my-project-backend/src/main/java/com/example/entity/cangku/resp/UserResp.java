package com.example.entity.cangku.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/9 23:19
 */
@Data
public class UserResp {
    Long id;
    Long tenantId;
    Long secrecyId;
    String username;
    String password;
    String nickname;
    Integer sex;
    Long industryCode;
    String avatarUrl;
    String email;
    String phone;
    String role;
    Date registerTime;
    String province;
    String city;
    String county;
}
