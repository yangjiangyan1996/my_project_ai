package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Inventory;
import com.example.entity.cangku.req.InventoryListPageReq;
import com.example.mapper.CkInventoryMapper;
import com.example.service.CkInventoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

// CkInventoryServiceImpl.java
@Service
public class CkInventoryServiceImpl extends ServiceImpl<CkInventoryMapper, Inventory> implements CkInventoryService {


    @Override
    public Inventory getByProduct(Long productId, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<Inventory>()
                .eq("product_id", productId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0));
    }


    @Override
    public Page<Inventory> getPage(Page<Inventory> page, InventoryListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<Inventory>()
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .in(!CollectionUtils.isEmpty(req.getProductIdsOfSku()), "product_id", req.getProductIdsOfSku())
                        .in(!CollectionUtils.isEmpty(req.getProductIdsOfName()), "product_id", req.getProductName())
                        .orderByAsc("modified_at")
        );
    }

    @Override
    public List<Inventory> selectByProductIds(Long tenantId, List<Long> productIds) {
        return baseMapper.selectList(new QueryWrapper<Inventory>()
                .eq("tenant_id", tenantId)
                .in("product_id", productIds)
                .eq("is_deleted", 0));
    }


    @Override
    public List<Inventory> selectByTenantId(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<Inventory>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0));
    }

    @Override
    public Long selectPageListCount(InventoryListPageReq req) {
        return baseMapper.selectPageListCount(req);
    }
}