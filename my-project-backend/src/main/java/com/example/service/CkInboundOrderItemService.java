package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.InboundOrderItem;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/1 23:16
 */
public interface CkInboundOrderItemService extends IService<InboundOrderItem> {
    List<InboundOrderItem> selectByTenantIdAndInboundOrderIds(Long tenantId, List<Long> inboundOrderIds);

    List<InboundOrderItem> selectByInboundOrderId(Long tenantId, Long orderId);

    public boolean deleteByOrderId(Long tenantId, Long orderId, Long userId);

    List<InboundOrderItem> selectByProductIds(Long tenantId, List<Long> productIds);
}
