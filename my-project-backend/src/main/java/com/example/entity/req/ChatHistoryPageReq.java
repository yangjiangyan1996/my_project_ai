package com.example.entity.req;

import com.example.entity.base.PageReq;
import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 11:58
 */
@Data
public class ChatHistoryPageReq extends PageReq {
    private Long secrecyId;
    private Long targetUserId;
    private Long currentUserId;
}
