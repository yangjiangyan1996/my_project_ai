package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.OutboundOrderItemSaleExt;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/9 10:18
 */
public interface CkOutboundOrderItemSaleExtService extends IService<OutboundOrderItemSaleExt> {
    List<OutboundOrderItemSaleExt> selectByOrderId(Long orderId, Long tenantId);
}
