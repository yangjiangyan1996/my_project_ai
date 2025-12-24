package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.ProductImage;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/24 10:35
 */
public interface CkProductImageService extends IService<ProductImage> {
    List<ProductImage> selectByProductIds(Long tenantId, List<Long> productIds);

    Boolean deletedByProductId(Long id, Long userId, Long tenantId);
}
