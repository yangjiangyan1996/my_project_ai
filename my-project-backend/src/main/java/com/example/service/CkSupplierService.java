package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Supplier;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/31 00:40
 */
public interface CkSupplierService extends IService<Supplier> {
    Supplier selectByTenantId(Long tenantId, String supplierCode);
}
