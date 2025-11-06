package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.ProductBom;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkProductBomMapper;
import com.example.service.CkProductBomService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 23:36
 */
@Service
public class CkProductBomServiceImpl  extends ServiceImpl<CkProductBomMapper, ProductBom> implements CkProductBomService {
    @Override
    public ProductBom selectByBomId(Long bomId, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<ProductBom>()
                .eq("id", bomId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<ProductBom> selectByBomIds(List<Long> bomIds, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<ProductBom>()
                .in("id", bomIds)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }
}
