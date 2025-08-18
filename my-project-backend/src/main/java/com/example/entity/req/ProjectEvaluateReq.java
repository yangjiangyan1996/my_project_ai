package com.example.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/18 10:26
 */
@Data
public class ProjectEvaluateReq {
    @NotNull(message = "被评价用户id不能为空")
    Long toUserId;
    @NotNull(message = "项目id不能为空")
    Long projectId;
    @NotNull(message = "评分不能为空")
    Integer score;
    @NotNull(message = "评价内容不能为空")
    String comment;
}
