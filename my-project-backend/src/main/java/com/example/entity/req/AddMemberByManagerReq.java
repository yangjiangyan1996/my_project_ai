package com.example.entity.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/4 11:34
 */
@Data
public class AddMemberByManagerReq {
    Long projectId;
    Long userId;
    Integer role;
}
