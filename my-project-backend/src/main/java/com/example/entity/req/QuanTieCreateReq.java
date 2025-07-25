package com.example.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 14:38
 */
@Data
public class QuanTieCreateReq {
    @NotBlank(message = "标题不能为空")
    String title;
    @NotBlank(message = "内容不能为空")
    String content;
    String avatar;
    @NotNull(message = "barsId不能为空")
    Long barId;
}
