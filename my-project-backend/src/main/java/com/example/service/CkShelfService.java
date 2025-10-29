package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.WarehouseShelf;
import com.example.entity.cangku.req.ShelfListPageReq;

import java.util.List;

// CkShelfService.java
public interface CkShelfService extends IService<WarehouseShelf> {
    List<WarehouseShelf> selectByCodeOrName(String shelfCode, String shelfName,Long tenantId);

    Page<WarehouseShelf> getPage(Page<WarehouseShelf> page, ShelfListPageReq req);
}