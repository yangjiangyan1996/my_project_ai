package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Unit;
import com.example.entity.cangku.req.UnitListPageReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 21:57
 */
public interface CkUnitService extends IService<Unit> {
    List<Unit> selectByTenantId(Long tenantId, int status);

    List<Unit> selectByTenantIdAndCodes(Long tenantId, List<String> unitCodeList);

    Page<Unit> getPage(Page<Unit> page, UnitListPageReq req);

    List<Unit> selectByCodeOrName(Long tenantId, String unitCode, String unitName);
}
