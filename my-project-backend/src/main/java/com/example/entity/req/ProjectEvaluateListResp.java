package com.example.entity.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/18 14:33
 */
@Data
public class ProjectEvaluateListResp {
    private Long reviewId;
    private Long projectId;
    private Long userId;
    Integer score;
    String comment;

}
