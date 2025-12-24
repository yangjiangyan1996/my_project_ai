package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.ProductImage;
import com.example.mapper.CkProductImageMapper;
import com.example.service.CkProductImageService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/24 10:35
 */
@Service
public class CkProductImageServiceImpl extends ServiceImpl<CkProductImageMapper, ProductImage> implements CkProductImageService {
    @Override
    public Boolean deletedByProductId(Long id, Long userId, Long tenantId) {
        ProductImage productImage = new ProductImage();
        productImage.setIsDeleted(1);
        productImage.setModifiedBy(userId);
        productImage.setModifiedAt(new Date());
        return baseMapper.update(productImage, new QueryWrapper<ProductImage>().eq("product_id", id)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0)) > 0;
    }

    @Override
    public List<ProductImage> selectByProductIds(Long tenantId, List<Long> productIds) {
        return baseMapper.selectList(new QueryWrapper<ProductImage>().eq("is_deleted", 0)
                .eq("tenant_id", tenantId)
                .in("product_id", productIds));
    }
}
