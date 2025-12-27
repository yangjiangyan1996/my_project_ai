package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Warehouse;
import com.example.entity.cangku.req.WareHouseListPageReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:27
 */
public interface CkWareHouseService extends IService<Warehouse> {
    Page<Warehouse> getPage(Page<Warehouse> page, WareHouseListPageReq req);

    List<Warehouse> listWareHouse(Long tenantId);

    List<Warehouse> selectByCodeOrName(String code, String name, Long tenantId);

    List<Warehouse> getByIds(List<Long> whIds, Long tenantId);

    List<Warehouse> listWareHouseEnable(Long tenantId);

    List<Warehouse> selectByTenantIdAndWareHouseIds(Long tenantId, List<Long> wareHouseIds);
    Warehouse selectByTenantIdAndWareHouseId(Long tenantId,Long wareHouseId);

    List<Warehouse> selectByTenantId(Long tenantId);

    Long selectCountsOfWareHouses(Long tenantId);
}
