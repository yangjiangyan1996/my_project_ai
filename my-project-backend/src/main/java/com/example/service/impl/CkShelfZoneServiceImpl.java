package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.ShelfZone;
import com.example.mapper.CkShelfMapper;
import com.example.service.CkShelfZoneService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

// CkShelfZoneServiceImpl.java
@Service
public class CkShelfZoneServiceImpl extends ServiceImpl<CkShelfMapper, ShelfZone> implements CkShelfZoneService {

    @Override
    public List<ShelfZone> selectByCodeOrName(String shelfCode, String shelfName, Long tenantId) {
        // 构造查询条件
        LambdaQueryWrapper<ShelfZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShelfZone::getTenantId, tenantId)
                .and(wrapper -> wrapper
                        .eq(StringUtils.isNotBlank(shelfCode), ShelfZone::getShelfCode, shelfCode)
                        .or()
                        .eq(StringUtils.isNotBlank(shelfName), ShelfZone::getShelfName, shelfName));

        // 查询结果
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateDeletedByIds(Set<Long> deletedList, Long tenantId, Long userId) {
        ShelfZone shelfZone = new ShelfZone();
        shelfZone.setIsDeleted(1);
        shelfZone.setModifiedBy(userId);
        shelfZone.setModifiedAt(new Date());
        return baseMapper.update(shelfZone,
                new QueryWrapper<ShelfZone>().in("id", deletedList)) > 0;
    }

    @Override
    public List<ShelfZone> selectByParentId(Long tenantId, Long parentId) {
        return
                baseMapper.selectList(new QueryWrapper<ShelfZone>()
                        .eq("is_deleted", 0)
                        .eq("tenant_id", tenantId)
                        .eq("parent_id", parentId));
    }

    @Override
    public List<ShelfZone> selectByParentIds(Long tenantId, List<Long> shelivesIds) {
        return baseMapper.selectList(new QueryWrapper<ShelfZone>()
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId)
                .in("parent_id", shelivesIds));
    }

    @Override
    public List<ShelfZone> selectByTenantId(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<ShelfZone>()
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId));
    }

    @Override
    public List<ShelfZone> listWareHouseEnable(Long tenantId, Long warehouseId) {
        return baseMapper.selectList(new QueryWrapper<ShelfZone>()
                .eq("is_deleted", 0)
                .eq("status", 0)
                .eq("warehouse_id", warehouseId));
    }

//    @Override
//    public Page<ShelfZone> getPage(Page<ShelfZone> page, ShelfListPageReq req) {
//        return baseMapper.selectPage(
//                page,
//                new QueryWrapper<ShelfZone>()
//                        .eq(req.getStatus()!= null ,"status", req.getStatus())
//                        .eq(req.getWarehouseId()!= null ,"warehouse_id", req.getWarehouseId())
//                        .like(StringUtils.isNotBlank(req.get() ), "area", req.getArea())
//                        .like(StringUtils.isNotBlank(req.getShelfCode() ), "shelf_code", req.getShelfCode())
//                        .like(StringUtils.isNotBlank(req.getShelfName()) , "shelf_name", req.getShelfName())
//                        .eq( "tenant_id", req.getTenantId())
//                        .eq("is_deleted",0)
//                        .orderByAsc("sort_order")
//        );
//    }

}