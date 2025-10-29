package com.example.entity.cangku.req;

import com.example.entity.base.PageReq;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 17:59
 */
@Data
public class ShelfListPageReq extends PageReq {
    String code;
    String name;
    Integer status;

    Long userId;
    Long tenantId;
}
