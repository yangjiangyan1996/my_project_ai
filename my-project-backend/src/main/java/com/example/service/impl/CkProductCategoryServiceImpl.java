// CkProductCategoryServiceImpl.java
package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.ProductCategory;
import com.example.mapper.CkProductCategoryMapper;
import com.example.service.CkProductCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CkProductCategoryServiceImpl extends ServiceImpl<CkProductCategoryMapper, ProductCategory> implements CkProductCategoryService {

    @Override
    public List<ProductCategory> selectByTenantId(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<ProductCategory>().eq("tenant_id", tenantId)
                .eq("is_deleted", 0));
    }

    @Override
    public ProductCategory selectById(Long tenantId, Long id) {
        return baseMapper.selectOne(new QueryWrapper<ProductCategory>()
                .eq("tenant_id", tenantId)
                .eq("id", id)
                .eq("is_deleted", 0));
    }

    @Override
    public ProductCategory selectByTenantIdAndCode(Long tenantId, String code) {
        return baseMapper.selectOne(new QueryWrapper<ProductCategory>()
                .eq("tenant_id", tenantId)
                .eq("category_code", code)
                .eq("is_deleted", 0));
    }

    @Override
    public List<ProductCategory> selectByTenantIdAndCodes(Long tenantId, List<String> categoryCodes) {
        return baseMapper.selectList(new QueryWrapper<ProductCategory>().eq("tenant_id", tenantId)
                .in("category_code", categoryCodes)
                .eq("is_deleted", 0));
    }
}