package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InventoryTransaction;
import com.example.mapper.CkInventoryTransactionMapper;
import com.example.service.CkInventoryTransactionService;
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
    public List<InventoryTransaction> selectByProductIds(Long tenantId, List<Long> productIds) {
        return baseMapper.selectList(new QueryWrapper<InventoryTransaction>()
                .eq("tenant_id", tenantId)
                .in("product_id", productIds)
                .eq("is_deleted", 0)
        );
    }
}