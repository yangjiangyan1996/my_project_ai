package com.example.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InventoryShelf;
import com.example.mapper.CkInventoryShelfMapper;
import com.example.service.CkInventoryShelfService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/15 22:43
 */
@Service
public class CkInventoryShelfServiceImpl extends ServiceImpl<CkInventoryShelfMapper, InventoryShelf> implements CkInventoryShelfService {
    @Override
    public InventoryShelf getByTenantWarehouseProductShelfBatch(Long tenantId, Long warehouseId, Long productId, Long shelfId, String batchNo) {
        return baseMapper.selectOne(new QueryWrapper<InventoryShelf>()
                .eq("tenant_id", tenantId)
                .eq("warehouse_id", warehouseId)
                .eq("product_id", productId)
                .eq("shelf_id", shelfId)
                .eq(StringUtils.isNotBlank(batchNo), "batch_no", batchNo)
                .eq("is_deleted", 0)
        );
    }

    @Override
    public List<InventoryShelf> selectByShelfIds(Long tenantId, List<Long> shelfIds) {
        return baseMapper.selectList(new QueryWrapper<InventoryShelf>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0)
                .in("shelf_id", shelfIds)
        );
    }

    @Override
    public List<InventoryShelf> getBatchShelfStock(Long tenantId, Long warehouseId, Long productId) {
        return baseMapper.selectList(new QueryWrapper<InventoryShelf>()
                .eq("tenant_id", tenantId)
                .eq("warehouse_id", warehouseId)
                .eq("product_id", productId)
                .eq("is_deleted", 0)
        );
    }

    @Override
    public List<InventoryShelf> getBatchShelfStock(Long tenantId, Long warehouseId, Set<Long> componentProductIds) {
        if (CollUtil.isEmpty(componentProductIds)) {
            return CollUtil.newArrayList();
        }
        return baseMapper.selectList(new QueryWrapper<InventoryShelf>()
                .eq("tenant_id", tenantId)
//                .eq("warehouse_id", warehouseId)
                .eq("is_deleted", 0)
                .in("product_id", componentProductIds)
        );
    }

    @Override
    public List<InventoryShelf> selectByProductIds(List<Long> productId, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<InventoryShelf>()
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId)
                .in("product_id", productId)
        );
    }

    @Override
    public List<InventoryShelf> selectByProductId(Long productId, Long tenantId) {
        return  baseMapper.selectList(new QueryWrapper<InventoryShelf>()
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId)
                .eq("product_id", productId)
        );
    }

    @Override
    public InventoryShelf getByWarehouseAndProductAndShelf(Long warehouseId, Long productId, Long shelfLocationId, String batchNo, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<InventoryShelf>()
                .eq("warehouse_id", warehouseId)
                .eq("product_id", productId)
                .eq("shelf_id", shelfLocationId)
                .eq(StringUtils.isNotBlank(batchNo), "batch_no", batchNo)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0)
        );
    }

    @Override
    public List<InventoryShelf> selectByTenantId(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<InventoryShelf>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0)
        );
    }
}
