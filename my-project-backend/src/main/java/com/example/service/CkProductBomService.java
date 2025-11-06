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

    Boolean deltedbyproductid(Long productId, Long userId, Long tenantId);

    ProductBom selectByProduectId(Long productId, Long tenantId);
}
