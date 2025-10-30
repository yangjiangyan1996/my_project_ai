package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Product;
import com.example.entity.cangku.req.ProductListPageReq;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 21:52
 */
public interface CkProductService extends IService<Product> {
    Product selectByTenantId(Long tenantId, String skuCode);

    Page<Product> getPage(Page<Product> page, ProductListPageReq req);
}
