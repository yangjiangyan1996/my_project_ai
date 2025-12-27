package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockTakeItem;
import com.example.entity.cangku.req.StockItemListPageReq;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkStockTakeItemMapper;
import com.example.service.CkStockTakeItemService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:54
 */
@Service
public class CkStockTakeItemServiceImpl extends ServiceImpl<CkStockTakeItemMapper, StockTakeItem> implements CkStockTakeItemService {
    @Override
    public List<StockTakeItem> selectByStockTakeId(Long stockTakeId, Long tenantId) {
        return this.baseMapper.selectList(
                new LambdaQueryWrapper<StockTakeItem>()
                        .eq(StockTakeItem::getStockTakeId, stockTakeId)
                        .eq(StockTakeItem::getTenantId, tenantId)
                        .eq(StockTakeItem::getIsDeleted, CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<StockTakeItem> selectByStockTakeIds(List<Long> stockTakeIds, Long tenantId) {
        return this.baseMapper.selectList(
                new QueryWrapper<StockTakeItem>()
                        .eq("tenant_id", tenantId)
                        .in("stock_take_id", stockTakeIds)
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public Boolean updateByStockTakeId(Long stockTakeId, Integer status, Long userId, Long tenantId) {
        StockTakeItem stockTakeItem = new StockTakeItem();
        stockTakeItem.setModifiedBy(userId);
        stockTakeItem.setModifiedAt(new Date());
        stockTakeItem.setStatus(status);
        return this.baseMapper.update(
                stockTakeItem,
                new QueryWrapper<StockTakeItem>()
                        .eq("tenant_id", tenantId)
                        .eq("stock_take_id", stockTakeId)
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())) > 0;
    }

    @Override
    public StockTakeItem selectByStockTakeIdAndItemId(Long stockTakeId, Long stockItemId, Long productId, Long tenantId) {
        return this.baseMapper.selectOne(
                new QueryWrapper<StockTakeItem>()
                        .eq("tenant_id", tenantId)
                        .eq("stock_take_id", stockTakeId)
                        .eq("id", stockItemId)
                        .eq("product_id", productId)
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public Page<StockTakeItem> getStockItemPageList(Page<StockTakeItem> page, StockItemListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<StockTakeItem>()
                        .eq("tenant_id", req.getTenantId())
                        .eq("stock_take_id", req.getStockTakeId())
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                        .orderByAsc("stock_take_id")
        );
    }

    @Override
    public List<StockTakeItem> selectCountByStockTakeId(Long stockTakeId) {
        return this.baseMapper.selectList(
                new LambdaQueryWrapper<StockTakeItem>()
                        .eq(StockTakeItem::getStockTakeId, stockTakeId)
                        .eq(StockTakeItem::getIsDeleted, CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }
}
