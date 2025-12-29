package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.AdjustOrder;
import com.example.entity.cangku.req.AdjustListPageReq;
import com.example.mapper.CkAdjustOrderMapper;
import com.example.service.CkAdjustOrderService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/29 15:22
 */
@Service
public class CkAdjustOrderServiceImpl extends ServiceImpl<CkAdjustOrderMapper, AdjustOrder> implements CkAdjustOrderService {
    @Override
    public Page<AdjustOrder> getStockPage(Page<AdjustOrder> page, AdjustListPageReq req) {
        return baseMapper.selectPage(page, new QueryWrapper<AdjustOrder>()
                .eq("tenant_id", req.getTenantId())
                .eq(ObjectUtils.isNotEmpty(req.getAdjustNo()),"adjust_no", req.getAdjustNo())
                .eq(ObjectUtils.isNotEmpty(req.getAdjustType()),"adjust_type", req.getAdjustType())
                .eq(ObjectUtils.isNotEmpty(req.getSourceType()),"source_type", req.getSourceType())
                .eq(ObjectUtils.isNotEmpty(req.getAdjustStatus()),"adjust_status", req.getAdjustStatus())
        );
    }
}
