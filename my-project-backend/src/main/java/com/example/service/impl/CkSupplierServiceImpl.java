package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Supplier;
import com.example.entity.cangku.req.SupplierListPageReq;
import com.example.mapper.CkSupplierMapper;
import com.example.service.CkSupplierService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/31 00:41
 */
@Service
public class CkSupplierServiceImpl extends ServiceImpl<CkSupplierMapper, Supplier> implements CkSupplierService {
    @Override
    public Supplier selectByTenantId(Long tenantId, String supplierCode) {
        return baseMapper.selectOne(new QueryWrapper<Supplier>()
                .eq("tenant_id", tenantId)
                .eq("supplier_code", supplierCode));
    }

    @Override
    public List<Supplier> selectByTenantIdAndSupplierIds(Long tenantId, List<Long> supplierIds) {
        if (supplierIds == null || supplierIds.isEmpty()) {
            return new ArrayList<>();
        }
        return baseMapper.selectList(new QueryWrapper<Supplier>()
                .eq("tenant_id", tenantId)
                .in("id", supplierIds));
    }

    @Override
    public List<Supplier> listWareHouseEnable(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<Supplier>()
                .eq("is_deleted", 0)
                .eq("status", 1)
                .eq("tenant_id", tenantId));
    }

    @Override
    public Supplier selectByCodeOrName(String supplierCode, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<Supplier>()
                .eq("tenant_id", tenantId)
                .eq("supplier_code", supplierCode)
                .eq("is_deleted", 0));
    }

    @Override
    public Page<Supplier> getPage(Page<Supplier> page, SupplierListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<Supplier>()
                        .eq(req.getStatus()!= null ,"status", req.getStatus())
                        .like(StringUtils.isNotBlank(req.getContactPerson() ) ,"contact_person", req.getContactPerson())
                        .like(StringUtils.isNotBlank(req.getSupplierCode() ), "supplier_code", req.getSupplierCode())
                        .like(StringUtils.isNotBlank(req.getSupplierName() ), "supplier_name", req.getSupplierName())
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .orderByAsc("created_at")
        );
    }
}
