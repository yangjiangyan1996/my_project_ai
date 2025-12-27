package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.StockTakeItem;
import com.example.entity.cangku.req.StockItemListPageReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:53
 */
public interface CkStockTakeItemService extends IService<StockTakeItem> {
    List<StockTakeItem> selectCountByStockTakeId(Long stockTakeId);

    List<StockTakeItem> selectByStockTakeId(Long stockTakeId, Long tenantId);

    Page<StockTakeItem> getStockItemPageList(Page<StockTakeItem> page, StockItemListPageReq req);

    StockTakeItem selectByStockTakeIdAndItemId(Long stockTakeId, Long stockItemId,Long productId, Long tenantId);

    Boolean updateByStockTakeId(Long stockTakeId, Integer status, Long userId, Long tenantId);

    List<StockTakeItem> selectByStockTakeIds(List<Long> stockTakeIds, Long tenantId);
}
