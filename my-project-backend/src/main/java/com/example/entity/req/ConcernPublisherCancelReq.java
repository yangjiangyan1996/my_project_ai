package com.example.entity.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 16:30
 */
@Data
public class ConcernPublisherCancelReq {
    //被关注的id
    private Long followeeId;
}
