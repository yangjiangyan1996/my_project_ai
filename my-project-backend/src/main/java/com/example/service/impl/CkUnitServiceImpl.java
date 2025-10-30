package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Unit;
import com.example.mapper.CkUnitMapper;
import com.example.service.CkUnitService;
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
    public List<Unit> selectByTenantIdAndCodes(Long tenantId, List<String> unitCodeList) {
        return baseMapper.selectList(new QueryWrapper<Unit>().eq("tenant_id", tenantId)
                .in("unit_code", unitCodeList)
                .eq("status", 1)
                .eq("is_deleted", 0));
    }
}
