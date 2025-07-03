package com.example.entity.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/3 11:18
 */
@Data
public class MyMemberGroupsResp {
    private Long id;
    private String name;
    private String roleOfMemberGroup;
    private java.util.Date createdAt;
}
