package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Product;
import com.example.entity.cangku.req.ProductListPageReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 21:52
 */
public interface CkProductService extends IService<Product> {
    Product selectByTenantId(Long tenantId, String skuCode);

    Page<Product> getPage(Page<Product> page, ProductListPageReq req);

    List<Product> listWareHouseEnable(Long tenantId);

    List<Product> selectByIds(Long tenantId, List<Long> productIds);
    Product selectById(Long tenantId, Long productId);

    List<Product> selectBySkus(Long tenantId, List<String> skuList);

    List<Product> selectByProductNameLike(Long tenantId, String productName);
}
