package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Warehouse;
import com.example.entity.cangku.req.WareHouseListPageReq;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:27
 */
public interface CkWareHouseService extends IService<Warehouse> {
    Page<Warehouse> getPage(Page<Warehouse> page, WareHouseListPageReq req);
}
