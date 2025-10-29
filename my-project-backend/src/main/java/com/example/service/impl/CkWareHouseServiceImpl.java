package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Warehouse;
import com.example.entity.cangku.req.WareHouseListPageReq;
import com.example.mapper.CkWareHouseMapper;
import com.example.service.CkWareHouseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:27
 */
@Service
public class CkWareHouseServiceImpl extends ServiceImpl<CkWareHouseMapper, Warehouse> implements CkWareHouseService {
    @Override
    public Page<Warehouse> getPage(Page<Warehouse> page, WareHouseListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<Warehouse>()
                        .eq("status", req.getStatus())
                        .like(StringUtils.isNotBlank(req.getCode() ), "code", req.getCode())
                        .like(StringUtils.isNotBlank(req.getName()) , "name", req.getName())
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .orderByDesc("created_at")
        );
    }
}
