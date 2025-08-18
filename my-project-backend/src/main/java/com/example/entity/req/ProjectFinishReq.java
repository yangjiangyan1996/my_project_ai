package com.example.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/17 15:50
 */
@Data
public class ProjectFinishReq {
    @NotNull(message = "项目id不能为空")
    private Long projectId;
}
