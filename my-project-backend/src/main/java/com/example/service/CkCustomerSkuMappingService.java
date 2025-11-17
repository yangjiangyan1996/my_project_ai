package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.CustomerSkuMapping;
import com.example.entity.cangku.req.SkuListPageReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/16 23:14
 */
public interface CkCustomerSkuMappingService extends IService<CustomerSkuMapping> {
    Page<CustomerSkuMapping> getPage(Page<CustomerSkuMapping> page, SkuListPageReq req);

    List<CustomerSkuMapping> selectByCustomerId(Long customerId, Long tenantId);

    CustomerSkuMapping selectByCustomerIdAndCustomerSku(Long tenantId,String productSku, Long customerId, String customerSku);
}
