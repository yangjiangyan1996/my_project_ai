
package com.example.entity.req;

import com.example.entity.base.PageReq;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 11:58
 */
@Data
public class ChatListPageReq extends PageReq {
    private Long currentUserId;
}
