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
public class SkuListPageReq extends PageReq {
    private Long customerId;
    private String customerSku;
    private String productName;
    private String productSku;
    private Integer status;

    private List<String> productSkus;

    Long userId;
    Long tenantId;
}
