package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.CustomerSkuMapping;
import com.example.entity.cangku.req.SkuListPageReq;
import com.example.mapper.CkCustomerSkuMappingMapper;
import com.example.service.CkCustomerSkuMappingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/16 23:14
 */
@Service
public class CkCustomerSkuMappingServiceImpl extends ServiceImpl<CkCustomerSkuMappingMapper, CustomerSkuMapping> implements CkCustomerSkuMappingService {
    @Override
    public CustomerSkuMapping selectByCustomerIdAndCustomerSku(Long tenantId,String productSku, Long customerId, String customerSku) {
        return baseMapper.selectOne(
                new QueryWrapper<CustomerSkuMapping>()
                        .eq("customer_id", customerId)
                        .eq("customer_sku", customerSku)
                        .eq("product_sku", productSku)
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public List<CustomerSkuMapping> selectByCustomerId(Long customerId, Long tenantId) {
        return baseMapper.selectList(
                new QueryWrapper<CustomerSkuMapping>()
                        .eq("customer_id", customerId)
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public Page<CustomerSkuMapping> getPage(Page<CustomerSkuMapping> page, SkuListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<CustomerSkuMapping>()
                        .eq(req.getStatus() != null, "status", req.getStatus())
                        .eq(!Objects.isNull(req.getCustomerId()), "customer_id", req.getCustomerId())
                        .eq(!StringUtils.isEmpty(req.getCustomerSku()), "customer_sku", req.getCustomerSku())
                        .eq(!StringUtils.isEmpty(req.getProductSku()), "product_sku", req.getProductSku())
                        .in(!CollectionUtils.isEmpty(req.getProductSkus()), "product_sku", req.getProductSkus())
                        .eq("tenant_id", req.getTenantId())
                        .eq("is_deleted", 0)
                        .orderByDesc("created_at")
        );
    }
}
