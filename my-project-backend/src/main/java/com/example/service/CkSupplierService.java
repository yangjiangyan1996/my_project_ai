package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Supplier;
import com.example.entity.cangku.req.SupplierListPageReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/31 00:40
 */
public interface CkSupplierService extends IService<Supplier> {
    Supplier selectByTenantId(Long tenantId, String supplierCode);

    Page<Supplier> getPage(Page<Supplier> page, SupplierListPageReq req);

    Supplier selectByCodeOrName(String supplierCode, Long tenantId);

    List<Supplier> listWareHouseEnable(Long tenantId);

    List<Supplier> selectByTenantIdAndSupplierIds(Long tenantId, List<Long> supplierIds);

    Supplier selectByTenantIdAndSupplierName(Long tenantId, String supplierName);
}
