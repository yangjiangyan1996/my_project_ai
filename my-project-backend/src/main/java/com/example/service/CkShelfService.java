//package com.example.service;
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.example.entity.cangku.dto.Shelives;
//import com.example.entity.cangku.dto.WarehouseShelf;
//import com.example.entity.cangku.req.ShelfListPageReq;
//
//import java.util.List;
//
//// CkShelfService.java
//public interface CkShelfService extends IService<WarehouseShelf> {
//    List<Shelives> selectByCodeOrName(String shelfCode, String shelfName, Long tenantId);
//
//    Page<Shelives> getPage(Page<Shelives> page, ShelfListPageReq req);
//
//    List<Shelives> listWareHouseEnable(Long tenantId, Long warehouseId);
//
//    List<Shelives> selectByTenantId(Long tenantId);
//
//    List<Shelives> selectByShelfId(List<Long> ids, Long tenantId);
//}