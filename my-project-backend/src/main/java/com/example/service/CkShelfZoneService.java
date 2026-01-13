package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.ShelfZone;

import java.util.List;
import java.util.Set;

// CkShelfZoneService.java
public interface CkShelfZoneService extends IService<ShelfZone> {
    List<ShelfZone> selectByCodeOrName(String shelfCode, String shelfName, Long tenantId);

//    Page<ShelfZone> getPage(Page<ShelfZone> page, ShelfListPageReq req);

    List<ShelfZone> listWareHouseEnable(Long tenantId, Long warehouseId);

    List<ShelfZone> selectByTenantId(Long tenantId);

    List<ShelfZone> selectByParentIds(Long tenantId, List<Long> shelivesIds);

    List<ShelfZone> selectByParentId(Long tenantId, Long parentId);

    boolean updateDeletedByIds(Set<Long> deletedList, Long tenantId, Long userId);
}