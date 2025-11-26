package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Unit;
import com.example.entity.cangku.req.UnitListPageReq;
import com.example.mapper.CkUnitMapper;
import com.example.service.CkUnitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 21:58
 */
@Service
public class CkUnitServiceImpl extends ServiceImpl<CkUnitMapper, Unit> implements CkUnitService {
    @Override
    public List<Unit> selectByTenantId(Long tenantId, int status) {
        return baseMapper.selectList(new QueryWrapper<Unit>().eq("tenant_id", tenantId)
                .eq("status", status)
                .eq("is_deleted", 0));
    }

    @Override
    public List<Unit> selectByCodeOrName(Long tenantId, String unitCode, String unitName) {
        return baseMapper.selectList(new QueryWrapper<Unit>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0)
                .and(wrapper -> wrapper
                        .eq("unit_code", unitCode)
                        .or()
                        .eq("unit_name", unitName)
                ));
    }

    @Override
    public Page<Unit> getPage(Page<Unit> page, UnitListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<Unit>()
                        .eq(req.getStatus()!= null ,"status", req.getStatus())
                        .like(StringUtils.isNotBlank(req.getUnitCode() ), "unit_code", req.getUnitCode())
                        .like(StringUtils.isNotBlank(req.getUnitName()) , "unit_name", req.getUnitName())
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .orderByAsc("id")
        );
    }

    @Override
    public List<Unit> selectByTenantIdAndCodes(Long tenantId, List<String> unitCodeList) {
        return baseMapper.selectList(new QueryWrapper<Unit>().eq("tenant_id", tenantId)
                .in("unit_code", unitCodeList)
                .eq("status", 1)
                .eq("is_deleted", 0));
    }
}
