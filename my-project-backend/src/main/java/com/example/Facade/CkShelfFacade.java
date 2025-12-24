package com.example.Facade;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.InventoryShelf;
import com.example.entity.cangku.dto.Warehouse;
import com.example.entity.cangku.dto.WarehouseShelf;
import com.example.entity.cangku.req.ShelfCreateReq;
import com.example.entity.cangku.req.ShelfDeleteReq;
import com.example.entity.cangku.req.ShelfListPageReq;
import com.example.entity.cangku.req.ShelfUpdateStatusReq;
import com.example.entity.cangku.resp.ShelfPageListResp;
import com.example.service.CkInventoryShelfService;
import com.example.service.CkShelfService;
import com.example.service.CkWareHouseService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 01:21
 */
@Service
@Slf4j
public class CkShelfFacade {
    @Resource
    CkInventoryShelfService inventoryShelfService;
    @Resource
    CkShelfService shelfService;
    @Resource
    CkWareHouseService wareHouseService;

    /**
     * 根据货架ID集合获取已经使用的容量
     * @param tenantId 租户ID
     * @param shelfIds 货架ID集合
     * @return key是shelfId， value是每个shelfId的quantity的和
     */
    public Map<Long , BigDecimal> getShelfUsedCapacity(Long tenantId, List<Long> shelfIds) {
        if (CollUtil.isEmpty(shelfIds)) {
            return new HashMap<>();
        }
        List<InventoryShelf> inventoryShelves = inventoryShelfService.selectByShelfIds(tenantId, shelfIds);
        //计算每个货架的 BigDecimal quantity 的和
        // 获取Map，key为shelfId，value为对应shelfId的quantity总和
        return  inventoryShelves.stream()
                .collect(Collectors.groupingBy(
                        InventoryShelf::getShelfId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                InventoryShelf::getQuantity,
                                BigDecimal::add
                        )
                ));
    }

    public Boolean create(ShelfCreateReq req) {
        //根据编号和名称去重
        List<WarehouseShelf> list = shelfService.selectByCodeOrName(req.getShelfCode(), req.getShelfName(), req.getTenantId());
        if (!CollectionUtils.isEmpty(list)) {
            throw new ValidationException("货架编码名称已存在");
        }
        if (req.getCapacity() <= 0) {
            throw new ValidationException("货架容量不能小于0");
        }

        WarehouseShelf save = new WarehouseShelf();
        BeanUtils.copyProperties(req, save);
        save.setCreatedAt(new Date());
        save.setCreatedBy(req.getUserId());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());

        return shelfService.save(save);
    }

    public Page<ShelfPageListResp> pageList(Page<WarehouseShelf> page, ShelfListPageReq req) {
        Page<WarehouseShelf> list = shelfService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<Long> whIds = list.getRecords().stream().map(v -> v.getWarehouseId()).collect(Collectors.toList());
        List<Warehouse> byIds = wareHouseService.getByIds(whIds, req.getTenantId());
        Map<Long, Warehouse> whMap = byIds.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));

        //处理使用率
        List<Long> shelfIds = list.getRecords().stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
        Map<Long, BigDecimal> shelfUsedCapacity = getShelfUsedCapacity(req.getTenantId(), shelfIds);

        List<ShelfPageListResp> collect = list.getRecords().stream().map(v -> {
            ShelfPageListResp p = new ShelfPageListResp();
            BeanUtils.copyProperties(v, p);
            BigDecimal utilizationRate = BigDecimal.valueOf(0);
            if (shelfUsedCapacity != null && shelfUsedCapacity.get(v.getId()) != null) {
                BigDecimal quantityOfUsed = shelfUsedCapacity.get(v.getId());
                utilizationRate = quantityOfUsed.divide(BigDecimal.valueOf(v.getCapacity()), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                p.setUtilizationQuantity(quantityOfUsed);
                p.setProductCount(quantityOfUsed);
            }

            p.setUtilizationRate(utilizationRate);
            p.setWarehouseName(whMap.get(v.getWarehouseId()).getName());
            return p;
        }).collect(Collectors.toList());

        Page<ShelfPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }


    public Boolean update(ShelfCreateReq req) {
        WarehouseShelf wh = shelfService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("仓库不存在");
        }
        List<WarehouseShelf> list = shelfService.selectByCodeOrName(req.getShelfCode(), req.getShelfName(), req.getTenantId());
        if (!CollectionUtils.isEmpty(list)) {
            for (WarehouseShelf warehouseShelf : list) {
                if (!warehouseShelf.getId().equals(req.getId())) {
                    throw new ValidationException("仓库编号和名称已存在");
                }
            }
        }
        if (req.getCapacity() <= 0) {
            throw new ValidationException("货架容量不能小于0");
        }
        WarehouseShelf save = new WarehouseShelf();
        BeanUtils.copyProperties(req, save);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());

        return shelfService.updateById(save);
    }


    public Boolean updateStatus(ShelfUpdateStatusReq req) {
        WarehouseShelf wh = shelfService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("仓库不存在");
        }

        WarehouseShelf save = new WarehouseShelf();
        save.setId(req.getId());
        save.setStatus(req.getStatus());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return shelfService.updateById(save);
    }

    public List<ShelfPageListResp> listEnable(Long tenantId, Long warehouseId) {
        List<WarehouseShelf> list = shelfService.listWareHouseEnable(tenantId,warehouseId);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        //获取货架剩余容量
        List<Long> shelfIds  = list.stream().map(v -> v.getId()).collect(Collectors.toList());
        Map<Long, BigDecimal> shelfId2AcailableCapacityMap = getShelfUsedCapacity(tenantId, shelfIds);

        return list.stream().map(v -> {
            ShelfPageListResp p = new ShelfPageListResp();
            BeanUtils.copyProperties(v, p);
            if (shelfId2AcailableCapacityMap.containsKey(v.getId())) {
                BigDecimal usedCapacity = shelfId2AcailableCapacityMap.getOrDefault(v.getId(), BigDecimal.ZERO);
                BigDecimal availableCapacity = new BigDecimal(v.getCapacity()).subtract(usedCapacity);
                p.setAvailableCapacity(availableCapacity);
            } else {
                p.setAvailableCapacity(new BigDecimal(v.getCapacity()));
            }
            //如果剩余容量小于0
            if (p.getAvailableCapacity().compareTo(BigDecimal.ZERO) <= 0) {
                p.setAvailableCapacity(BigDecimal.ZERO);
            }
            return p;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(ShelfDeleteReq req) {
        WarehouseShelf p = shelfService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("货架不存在");
        }

        Map<Long, BigDecimal> shelfUsedCapacity = getShelfUsedCapacity(req.getTenantId(), Collections.singletonList(req.getId()));
        if (shelfUsedCapacity != null
                && shelfUsedCapacity.get(req.getId()) != null
                && shelfUsedCapacity.get(req.getId()).compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException("货架有商品存在");
        }

        WarehouseShelf save = new WarehouseShelf();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return shelfService.updateById(save);
    }
}
