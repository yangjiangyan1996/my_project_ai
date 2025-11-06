package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.OutboundOrderItem;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkOutboundOrderItemMapper;
import com.example.service.CkOutboundOrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 00:41
 */
@Service
public class CkOutboundOrderItemServiceImpl extends ServiceImpl<CkOutboundOrderItemMapper, OutboundOrderItem> implements CkOutboundOrderItemService {
    @Override
    public List<OutboundOrderItem> selectByOrderId(Long id, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<OutboundOrderItem>().eq("order_id", id)
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId));
    }

    @Override
    public List<OutboundOrderItem> selectByTenantIdAndOutboundOrderIds(Long tenantId, List<Long> outboundOrderIds) {
        return baseMapper.selectList(
                new QueryWrapper<OutboundOrderItem>()
                        .eq("tenant_id", tenantId)
                        .in("order_id", outboundOrderIds)
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
        );
    }

    @Override
    public List<OutboundOrderItem> selectByProductIds(Long tenantId, List<Long> productIds) {
        return baseMapper.selectList(
                new QueryWrapper<OutboundOrderItem>()
                        .eq("tenant_id", tenantId)
                        .in("product_id", productIds)
                        .eq("is_deleted", 0)
        );
    }
}
