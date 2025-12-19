package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InboundOrderItem;
import com.example.mapper.CkInboundOrderItemMapper;
import com.example.service.CkInboundOrderItemService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/1 23:17
 */
@Service
public class CkInboundOrderItemServiceImpl extends ServiceImpl<CkInboundOrderItemMapper, InboundOrderItem> implements CkInboundOrderItemService {
    @Override
    public List<InboundOrderItem> selectByProductIds(Long tenantId, List<Long> productIds) {
        return baseMapper.selectList(new QueryWrapper<InboundOrderItem>().eq("is_deleted", 0)
                .in("product_id", productIds)
                .eq("tenant_id", tenantId));
    }

    @Override
    public boolean deleteByOrderId(Long tenantId, Long orderId, Long userId) {
        InboundOrderItem inboundOrderItem = new InboundOrderItem();
        inboundOrderItem.setIsDeleted(1);
        inboundOrderItem.setModifiedAt(new Date());
        inboundOrderItem.setModifiedBy(userId);
        return this.update(inboundOrderItem, new QueryWrapper<InboundOrderItem>().eq("order_id", orderId));
    }

    @Override
    public List<InboundOrderItem> selectByInboundOrderId(Long tenantId, Long orderId) {
        return this.getBaseMapper().selectList(new QueryWrapper<InboundOrderItem>().eq("tenant_id", tenantId).eq("order_id", orderId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<InboundOrderItem> selectByTenantIdAndInboundOrderIds(Long tenantId, List<Long> inboundOrderIds) {
        return baseMapper.selectList(new QueryWrapper<InboundOrderItem>().eq("is_deleted", 0)
                .in("order_id", inboundOrderIds)
                .eq("tenant_id", tenantId));
    }

    @Override
    public List<InboundOrderItem> selectByBatNoList(Long tenantId, List<String> batchNos) {
        return baseMapper.selectList(new QueryWrapper<InboundOrderItem>().eq("is_deleted", 0)
                .in("batch_no", batchNos)
                .eq("tenant_id", tenantId));
    }

    @Override
    public List<InboundOrderItem> selectByOrderItemIds(Long tenantId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(new QueryWrapper<InboundOrderItem>().eq("is_deleted", 0)
                .in("id", ids)
                .eq("tenant_id", tenantId));
    }
}

