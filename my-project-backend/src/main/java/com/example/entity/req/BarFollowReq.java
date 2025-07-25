package com.example.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 11:30
 */
@Data
public class BarFollowReq {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotNull(message = " barsId不能为空")
    private Long barId;
}
