package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InventoryTransaction;
import com.example.entity.cangku.req.InventoryTransactionListPageReq;
import com.example.mapper.CkInventoryTransactionMapper;
import com.example.service.CkInventoryTransactionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

// CkInventoryTransactionServiceImpl.java
@Service
public class CkInventoryTransactionServiceImpl extends ServiceImpl<CkInventoryTransactionMapper, InventoryTransaction> implements CkInventoryTransactionService {

    @Override
    public InventoryTransaction getByOrderAndProduct(Long tenantId, Long orderId, Long productId) {
        return this.getOne(new QueryWrapper<InventoryTransaction>()
                .eq("tenant_id", tenantId)
                .eq("order_id", orderId)
                .eq("product_id", productId)
                .eq("is_deleted", 0)
        );
    }

    @Override
    public List<InventoryTransaction> selectByProductId(Long tenantId, Long productId) {
        return baseMapper.selectList(new QueryWrapper<InventoryTransaction>()
                .eq("tenant_id", tenantId)
                .eq("product_id", productId)
                .eq("is_deleted", 0)
        );
    }

    @Override
    public List<InventoryTransaction> selectByProductIds(Long tenantId, List<Long> productIds) {
        return baseMapper.selectList(new QueryWrapper<InventoryTransaction>()
                .eq("tenant_id", tenantId)
                .in("product_id", productIds)
                .eq("is_deleted", 0)
        );
    }

    @Override
    public Page<InventoryTransaction> getPage(Page<InventoryTransaction> page, InventoryTransactionListPageReq req) {
        return baseMapper.selectPage(page, new QueryWrapper<InventoryTransaction>()
                        .eq("tenant_id", req.getTenantId())
                        .eq("is_deleted", 0)
                        .eq("product_id", req.getProductId())
                        .eq(req.getWarehouseId() != null, "warehouse_id", req.getWarehouseId())
                        .eq(req.getOrderType() != null, "order_type", req.getOrderType())
                        .ge(StringUtils.isNotBlank(req.getStartDate()), "created_at", req.getStartDate())
                        .le(StringUtils.isNotBlank(req.getEndDate()), "created_at", req.getEndDate())
                        .orderByDesc("created_at")
//                .eq("order_id", req.getOrderId())
//                .eq("order_item_id", req.getOrderItemId())
//                .eq("stock_status", req.getStockStatus())
        );
    }
}