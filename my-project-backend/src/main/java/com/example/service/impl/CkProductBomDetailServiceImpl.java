package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.ProductBomDetail;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkProductBomDetailMapper;
import com.example.service.CkProductBomDetailService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 23:39
 */
@Service
public class CkProductBomDetailServiceImpl extends ServiceImpl<CkProductBomDetailMapper, ProductBomDetail> implements CkProductBomDetailService {
    @Override
    public List<ProductBomDetail> selectByBomId(Long bomId, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<ProductBomDetail>()
                .eq("bom_id", bomId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete));
    }

    @Override
    public Boolean deletedByBomId(Long bomId, Long userId,  Long tenantId) {
        ProductBomDetail productBom = new ProductBomDetail();
        productBom.setModifiedAt(new Date());
        productBom.setModifiedBy(userId);
        productBom.setIsDeleted(CkCommonEnums.IsDeleted.Delete.getCode());


        return baseMapper.update(
                productBom,
                new QueryWrapper<ProductBomDetail>()
                        .eq("bom_id", bomId)
                        .eq("tenant_id", tenantId)) > 0;
    }

    @Override
    public List<ProductBomDetail> selectByBomIds(List<Long> bomIds, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<ProductBomDetail>()
                .in("bom_id", bomIds)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete));
    }
}
