package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Supplier;
import com.example.mapper.CkSupplierMapper;
import com.example.service.CkSupplierService;
import org.springframework.stereotype.Service;

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
}
