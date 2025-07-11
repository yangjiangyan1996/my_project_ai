package com.example.entity.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 16:30
 */
@Data
public class ConcernPublisherReq {
    //被关注的id
    @NotNull(message = "被关注的id不能为空")
    @Min(value = 1, message = "被关注的id不能小于1")
    private Long followeeId;
}
