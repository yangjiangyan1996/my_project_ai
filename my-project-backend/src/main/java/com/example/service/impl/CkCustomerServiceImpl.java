package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Customer;
import com.example.entity.cangku.req.CustomerListPageReq;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkCustomerMapper;
import com.example.service.CkCustomerService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 20:26
 */
@Service
public class CkCustomerServiceImpl extends ServiceImpl<CkCustomerMapper, Customer> implements CkCustomerService {
    @Override
    public Customer selectByCode(Long tenantId, String customerCode) {
        return baseMapper.selectOne(new QueryWrapper<Customer>()
                .eq("tenant_id", tenantId)
                .eq("customer_code", customerCode)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public Customer selectByName(Long tenantId, String customerName) {
        return baseMapper.selectOne(new QueryWrapper<Customer>()
                .eq("tenant_id", tenantId)
                .eq("customer_name", customerName)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public Page<Customer> getPage(Page<Customer> page, CustomerListPageReq req) {
        return baseMapper.selectPage(page, new QueryWrapper<Customer>()
                .eq("tenant_id", req.getTenantId())
                .eq(ObjectUtils.isNotEmpty(req.getStatus()),"status", req.getStatus())
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                .like(StringUtils.isNotBlank(req.getCustomerCode()), "customer_code", req.getCustomerCode())
                .like(StringUtils.isNotBlank(req.getCustomerName()), "customer_name", req.getCustomerName())
                .eq(ObjectUtils.isNotEmpty(req.getCustomerLevel()), "customer_level", req.getCustomerLevel())
                .eq(ObjectUtils.isNotEmpty(req.getCustomerType()), "customer_type", req.getCustomerType())
        );
    }

    @Override
    public Customer selectByTenantIdAndCustomerName(Long tenantId, String name) {
        return baseMapper.selectOne(new QueryWrapper<Customer>()
                .eq("tenant_id", tenantId)
                .eq("customer_name", name)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<Customer> selectByTenantIdAndCustomerIds(Long tenantId, List<Long> customerIds) {
        return baseMapper.selectList(new QueryWrapper<Customer>()
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId)
                .in("id", customerIds)
        );
    }

    @Override
    public List<Customer> listEnable(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<Customer>()
                .eq("tenant_id", tenantId)
                .eq("status", CkCommonEnums.Status.Enable.getCode())
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }
}
