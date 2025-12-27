package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.StockTake;
import com.example.entity.cangku.req.StockListPageReq;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:53
 */
public interface CkStockTakeService extends IService<StockTake> {

    Page<StockTake> getStockPage(Page<StockTake> page, StockListPageReq req);

    StockTake selectById(Long stockTakeId, Long tenantId);
}
