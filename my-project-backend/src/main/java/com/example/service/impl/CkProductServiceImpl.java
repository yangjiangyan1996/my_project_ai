package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Product;
import com.example.entity.cangku.req.ProductListPageReq;
import com.example.mapper.CkProductMapper;
import com.example.service.CkProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 21:53
 */
@Service
public class CkProductServiceImpl extends ServiceImpl<CkProductMapper, Product> implements CkProductService {
    @Override
    public List<Product> selectByTenantId(Long tenantId) {
        return this.baseMapper.selectList(new QueryWrapper<Product>().eq("tenant_id", tenantId).eq("is_deleted", 0));
    }

    @Override
    public Product selectBySku(Long tenantId, String skuCode) {
        return baseMapper.selectOne(new QueryWrapper<Product>().eq("tenant_id", tenantId)
                .eq("sku", skuCode)
                .eq("is_deleted", 0)
        );
    }

    @Override
    public Product selectById(Long tenantId, Long productId) {
        return baseMapper.selectOne(
                new QueryWrapper<Product>()
                        .eq("tenant_id", tenantId)
                        .eq("id", productId)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public Long selectCountsOfProducts(Long tenantId) {
        return baseMapper.selectCount(new QueryWrapper<Product>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<Product> selectByProductSkuLike(Long tenantId, String sku) {
        return baseMapper.selectList(
                new QueryWrapper<Product>()
                        .eq("tenant_id", tenantId)
                        .like("sku", sku)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public List<Product> selectByProductNameLike(Long tenantId, String productName) {
        return baseMapper.selectList(
                new QueryWrapper<Product>()
                        .eq("tenant_id", tenantId)
                        .like("name", productName)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public List<Product> selectBySkus(Long tenantId, List<String> skuList) {
        return baseMapper.selectList(
                new QueryWrapper<Product>()
                        .eq("tenant_id", tenantId)
                        .in("sku", skuList)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public List<Product> selectByIds(Long tenantId, List<Long> productIds) {
        return baseMapper.selectList(
                new QueryWrapper<Product>()
                        .eq("tenant_id", tenantId)
                        .in("id", productIds)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public List<Product> listWareHouseEnable(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<Product>().eq("is_deleted", 0)
                .eq("status", 1)
                .eq("tenant_id", tenantId));
    }

    @Override
    public Page<Product> getPage(Page<Product> page, ProductListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<Product>()
                        .eq(req.getStatus()!= null ,"status", req.getStatus())
                        .eq(StringUtils.isNotBlank(req.getCategoryCode() ), "category_code", req.getCategoryCode())
                        .like(StringUtils.isNotBlank(req.getName() ), "name", req.getName())
                        .like(StringUtils.isNotBlank(req.getSku()) , "sku", req.getSku())
                        .like(StringUtils.isNotBlank(req.getEnglishName()) , "english_name", req.getEnglishName())
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .orderByAsc("created_at")
        );
    }
}
