package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.OutboundOrderItem;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 00:39
 */


public interface CkOutboundOrderItemService extends IService<OutboundOrderItem> {
    List<OutboundOrderItem> selectByProductIds(Long tenantId, List<Long> productIds);
}
