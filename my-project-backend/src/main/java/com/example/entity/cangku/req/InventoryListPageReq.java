package com.example.entity.cangku.req;

import com.example.entity.base.PageReq;
import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 17:59
 */
@Data
public class InventoryListPageReq extends PageReq {
    private String productName;
    private String sku;

    private List<Long> productIdsOfSku;
    private List<Long> productIdsOfName;
    Long userId;
    Long tenantId;
}
