package com.example.entity.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/18 14:33
 */
@Data
public class ProjectAvgEvaluateListResp {
    Long userId;
    String userName;
    String avatar;
    Double avgScore;
    List<ProjectEvaluateResp> evaluateList;

    @Data
    public static class ProjectEvaluateResp {
        private String fromUserName;
        private String fromUserAvatar;
        Integer score;
        String comment;
    }
}
