package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.OutboundOrderItemSaleExt;
import com.example.mapper.CkOutboundOrderItemSaleExtMapper;
import com.example.service.CkOutboundOrderItemSaleExtService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/9 10:19
 */
@Service
public class CkOutboundOrderItemSaleExtServiceImpl extends ServiceImpl<CkOutboundOrderItemSaleExtMapper, OutboundOrderItemSaleExt> implements CkOutboundOrderItemSaleExtService {
    @Override
    public List<OutboundOrderItemSaleExt> selectByOrderId(Long orderId, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<OutboundOrderItemSaleExt>().eq("order_id", orderId)
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId));
    }
}
