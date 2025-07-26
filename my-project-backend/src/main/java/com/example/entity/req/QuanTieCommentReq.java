package com.example.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/21 16:40
 */
@Data
public class QuanTieCommentReq {
    @NotBlank(message = "内容不能为空")
    private String content;
    @NotNull(message = " tieId不能为空")
    private Long tieId;
    private Long replyTo;
    private Long firstLevelCommonId;
}
