package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.Inventory;
import com.example.entity.cangku.req.InventoryListPageReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// CkInventoryMapper.java
@Mapper
public interface CkInventoryMapper extends BaseMapper<Inventory> {
    List<Inventory> selectPageList(@Param("req") InventoryListPageReq req, Page<Inventory> page);

    // 添加计数查询方法
    Long selectPageListCount(@Param("req") InventoryListPageReq req);
}