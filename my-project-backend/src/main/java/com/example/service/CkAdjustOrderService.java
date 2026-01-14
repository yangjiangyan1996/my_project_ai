package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.AdjustOrder;
import com.example.entity.cangku.req.AdjustListPageReq;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/29 15:21
 */
public interface CkAdjustOrderService extends IService<AdjustOrder> {
    Page<AdjustOrder> getStockPage(Page<AdjustOrder> page, AdjustListPageReq req);

    AdjustOrder selectById(Long id, Long tenantId);

    boolean updateStatusById(Long id, Long tenantId, Integer status);

    boolean updateStatusAndRemarkById(Long id, Long tenantId, Integer status, String approveRemark);
}
