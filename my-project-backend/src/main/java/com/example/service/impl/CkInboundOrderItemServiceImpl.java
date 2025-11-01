package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InboundOrderItem;
import com.example.entity.cangku.dto.Product;
import com.example.mapper.CkInboundOrderItemMapper;
import com.example.service.CkInboundOrderItemService;
import org.springframework.stereotype.Service;

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
    public List<InboundOrderItem> selectByTenantIdAndInboundOrderIds(Long tenantId, List<Long> inboundOrderIds) {
        return baseMapper.selectList(new QueryWrapper<InboundOrderItem>().eq("is_deleted", 0)
                .in("order_id", inboundOrderIds)
                .eq("tenant_id", tenantId));
    }
}
