package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Shelives;
import com.example.entity.cangku.req.ShelfListPageReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2026/1/2 22:18
 */
public interface CkShelivesService extends IService<Shelives> {
    Page<Shelives> getPage(Page<Shelives> page, ShelfListPageReq req);

    List<Shelives> selectByIds(Long tenantId, List<Long> ids);

    Shelives selectById(Long id, Long tenantId);
}
