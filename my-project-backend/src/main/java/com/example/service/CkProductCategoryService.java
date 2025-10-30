// CkProductCategoryService.java
package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.ProductCategory;

import java.util.List;

public interface CkProductCategoryService extends IService<ProductCategory> {

    List<ProductCategory> selectByTenantId(Long tenantId);

    ProductCategory selectByTenantIdAndCode(Long tenantId, String code);

    List<ProductCategory> selectByTenantIdAndCodes(Long tenantId, List<String> categoryCodes);
}