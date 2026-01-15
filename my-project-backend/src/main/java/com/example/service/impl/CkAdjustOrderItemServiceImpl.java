package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.AdjustOrderItem;
import com.example.mapper.CkAdjustOrderItemMapper;
import com.example.service.CkAdjustOrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/29 15:22
 */
@Service
public class CkAdjustOrderItemServiceImpl extends ServiceImpl<CkAdjustOrderItemMapper, AdjustOrderItem> implements CkAdjustOrderItemService {
    @Override
    public List<AdjustOrderItem> selectByAdjustOrderId(Long adjustOrderId, Long tenantId) {
        return this.baseMapper.selectList(new QueryWrapper<AdjustOrderItem>()
                .eq("tenant_id", tenantId)
                .eq("adjust_order_id", adjustOrderId)
                .eq("is_deleted", 0));
    }
}
