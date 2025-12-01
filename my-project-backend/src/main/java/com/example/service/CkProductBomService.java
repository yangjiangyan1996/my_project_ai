package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.ProductBom;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 23:36
 */
public interface CkProductBomService extends IService<ProductBom> {
    ProductBom selectByBomId(Long bomId, Long tenantId);
    List<ProductBom> selectByBomIds(List<Long> bomIds, Long tenantId);

    Boolean deltedById(Long id, Long userId, Long tenantId);

    ProductBom selectByProduectId(Long productId, Long tenantId);
    List<ProductBom> selectByProduectIds(List<Long> productId, Long tenantId);

    List<ProductBom> selectByTenantId(Long tenantId);

    Integer deletedById(Long id, Long tenantId, Long userId);
}
