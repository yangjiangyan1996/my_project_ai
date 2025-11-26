package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.ProductBom;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkProductBomMapper;
import com.example.service.CkProductBomService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 23:36
 */
@Service
public class CkProductBomServiceImpl extends ServiceImpl<CkProductBomMapper, ProductBom> implements CkProductBomService {
    @Override
    public ProductBom selectByBomId(Long bomId, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<ProductBom>()
                .eq("id", bomId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<ProductBom> selectByBomIds(List<Long> bomIds, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<ProductBom>()
                .in("product_id", bomIds)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public Integer deletedById(Long id, Long tenantId, Long userId) {
        ProductBom p = new ProductBom();
        p.setModifiedAt(new Date());
        p.setModifiedBy(userId);
        p.setIsDeleted(CkCommonEnums.IsDeleted.Delete.getCode());
        return this.baseMapper.update(p, new QueryWrapper<ProductBom>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<ProductBom> selectByTenantId(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<ProductBom>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<ProductBom> selectByProduectIds(List<Long> productId, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<ProductBom>()
                .in("product_id", productId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public ProductBom selectByProduectId(Long productId, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<ProductBom>()
                .eq("product_id", productId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public Boolean deltedbyproductid(Long productId, Long userId, Long tenantId) {
        ProductBom productBom = new ProductBom();
        productBom.setModifiedAt(new Date());
        productBom.setModifiedBy(userId);
        productBom.setIsDeleted(CkCommonEnums.IsDeleted.Delete.getCode());


        return baseMapper.update(
                productBom,
                new QueryWrapper<ProductBom>()
                        .eq("product_id", productId)
                        .eq("tenant_id", tenantId)) > 0;
    }
}
