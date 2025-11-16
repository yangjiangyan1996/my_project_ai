package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.ProductBomDetail;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 23:39
 */
public interface CkProductBomDetailService  extends IService<ProductBomDetail> {
    List<ProductBomDetail> selectByBomId(Long bomId, Long tenantId);

    List<ProductBomDetail> selectByBomIds(List<Long> bomIds, Long tenantId);

    Boolean deletedByBomId(Long bomId, Long userId, Long tenantId);

    List<ProductBomDetail> selectByTenantId(Long tenantId);
}
