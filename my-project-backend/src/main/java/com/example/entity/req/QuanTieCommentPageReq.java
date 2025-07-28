package com.example.entity.req;

import com.example.entity.base.PageReq;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:58
 */
@Data
public class QuanTieCommentPageReq extends PageReq {
    @NotNull(message = " tieId不能为空")
    private Long tieId;

    //第一层的评论ID （二级评论分页）
    private Long firstCommenId;
    //预览2条回复
    private Integer previewReplyCount;
    //要求返回预览回复
    private Boolean withPreviewReplies;
}
