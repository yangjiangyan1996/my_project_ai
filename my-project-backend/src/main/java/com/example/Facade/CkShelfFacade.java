package com.example.Facade;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.InventoryShelf;
import com.example.entity.cangku.dto.ShelfZone;
import com.example.entity.cangku.dto.Shelives;
import com.example.entity.cangku.dto.Warehouse;
import com.example.entity.cangku.req.ShelfCreateReq;
import com.example.entity.cangku.req.ShelfListPageReq;
import com.example.entity.cangku.req.ShelfUpdateStatusReq;
import com.example.entity.cangku.req.ShelviesCreateWithZoneReq;
import com.example.entity.cangku.resp.ShelfPageListResp;
import com.example.entity.cangku.resp.ShelfZoneResp;
import com.example.service.CkInventoryShelfService;
import com.example.service.CkShelfZoneService;
import com.example.service.CkShelivesService;
import com.example.service.CkWareHouseService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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
    CkShelivesService shelivesService;
    @Resource
    CkInventoryShelfService inventoryShelfService;
    @Resource
    CkShelfZoneService shelfService;
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
            return null;
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
        List<ShelfZone> list = shelfService.selectByCodeOrName(req.getShelfCode(), req.getShelfName(), req.getTenantId());
        if (!CollectionUtils.isEmpty(list)) {
            throw new ValidationException("货架编码名称已存在");
        }
        if (req.getCapacity() <= 0) {
            throw new ValidationException("货架容量不能小于0");
        }

        ShelfZone save = new ShelfZone();
        BeanUtils.copyProperties(req, save);
        save.setCreatedAt(new Date());
        save.setCreatedBy(req.getUserId());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());

        return shelfService.save(save);
    }

    public Boolean createWithZones(ShelviesCreateWithZoneReq req) {
        Shelives save = new Shelives();
        BeanUtils.copyProperties(req.getShelf(), save);
        save.setRemark(req.getShelf().getRemark());
        save.setTenantId(req.getTenantId());
        save.setCreatedAt(new Date());
        save.setCreatedBy(req.getUserId());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        boolean save1 = shelivesService.save(save);
        if (!save1) {
            throw new ValidationException("货架创建失败");
        }
        List<ShelviesCreateWithZoneReq.ShelfDTO> zones = req.getZones();
        List<ShelfZone> shelfZoneStream = zones.stream().map(v -> {
            ShelfZone shelfZone = new ShelfZone();
            BeanUtils.copyProperties(v, shelfZone);
            shelfZone.setParentId(save.getId());
            shelfZone.setShelfCode(v.getZoneCode());
            shelfZone.setStatus(v.getStatus());
            shelfZone.setShelfName(v.getZoneName());
            shelfZone.setWarehouseId(save.getWarehouseId());
            shelfZone.setCapacity(v.getCapacity());
            shelfZone.setCapacityUnit(v.getCapacityUnit());
            shelfZone.setTenantId(req.getTenantId());
            shelfZone.setCreatedAt(new Date());
            shelfZone.setCreatedBy(req.getUserId());
            shelfZone.setModifiedAt(new Date());
            shelfZone.setModifiedBy(req.getUserId());
            return shelfZone;
        }).collect(Collectors.toList());
        //根据编号和名称去重

        return shelfService.saveBatch(shelfZoneStream);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWithZones(ShelviesCreateWithZoneReq req) {
        // 1. 验证货架是否存在
        Shelives existingShelf = shelivesService.getById(req.getShelf().getId());
        if (existingShelf == null) {
            throw new ValidationException("货架不存在，ID: " + req.getShelf().getId());
        }

        // 3. 更新货架基本信息
        Shelives updateShelf = new Shelives();
        BeanUtils.copyProperties(req.getShelf(), updateShelf);
        updateShelf.setId(existingShelf.getId()); // 确保ID不变
        updateShelf.setModifiedAt(new Date());
        updateShelf.setModifiedBy(req.getUserId());

        // 4. 更新货架
        boolean updateShelfResult = shelivesService.updateById(updateShelf);
        if (!updateShelfResult) {
            throw new ValidationException("货架更新失败");
        }

        List<ShelviesCreateWithZoneReq.ShelfDTO> waitUpdateZoneList = req.getZones();

        //查询zone, 然后对比要更新的
        List<ShelfZone> existingZones = shelfService.selectByParentId(existingShelf.getTenantId(), existingShelf.getId());
        Map<Long, ShelfZone> existingZoneMap = existingZones.stream().collect(Collectors.toMap(ShelfZone::getId, Function.identity()));

        List<ShelfZone> insertList = new ArrayList<>();
        List<ShelfZone> updateList = new ArrayList<>();
        Set<Long> deletedList= existingZoneMap.keySet();
        for (ShelviesCreateWithZoneReq.ShelfDTO shelfDTO : waitUpdateZoneList) {
            if (shelfDTO.getId() == null) {
                // 新增
                ShelfZone shelfZone = new ShelfZone();
                BeanUtils.copyProperties(shelfDTO, shelfZone);
                shelfZone.setParentId(updateShelf.getId());
                shelfZone.setStatus(shelfDTO.getStatus());
                shelfZone.setShelfCode(shelfDTO.getZoneCode());
                shelfZone.setShelfName(shelfDTO.getZoneName());
                shelfZone.setWarehouseId(updateShelf.getWarehouseId());
                shelfZone.setCapacity(shelfDTO.getCapacity());
                shelfZone.setCapacityUnit(shelfDTO.getCapacityUnit());
                shelfZone.setTenantId(req.getTenantId());
                shelfZone.setCreatedAt(new Date());
                shelfZone.setCreatedBy(req.getUserId());
                shelfZone.setModifiedAt(new Date());
                shelfZone.setModifiedBy(req.getUserId());
                insertList.add(shelfZone);
            }else if (shelfDTO.getId() != null) {
                ShelfZone existingZone = existingZoneMap.get(shelfDTO.getId());
                if (existingZone == null) {
                    throw new ValidationException("货架不存在，ID: " + shelfDTO.getId());
                }
                // 更新
                ShelfZone updateShelfZone = new ShelfZone();
                updateShelfZone.setId(existingZone.getId());
                updateShelfZone.setParentId(existingShelf.getId());
                updateShelfZone.setStatus(shelfDTO.getStatus());
                updateShelfZone.setShelfCode(shelfDTO.getZoneCode());
                updateShelfZone.setShelfName(shelfDTO.getZoneName());
                updateShelfZone.setWarehouseId(existingZone.getWarehouseId());
                updateShelfZone.setCapacity(shelfDTO.getCapacity());
                updateShelfZone.setCapacityUnit(shelfDTO.getCapacityUnit());
                updateShelfZone.setTenantId(req.getTenantId());
                updateShelfZone.setCreatedAt(new Date());
                updateShelfZone.setCreatedBy(req.getUserId());
                updateShelfZone.setModifiedAt(new Date());
                updateShelfZone.setModifiedBy(req.getUserId());
                updateList.add(updateShelfZone);

                deletedList.remove(shelfDTO.getId());

            }
        }

        if (!insertList.isEmpty()) {
            boolean insertResult = shelfService.saveBatch(insertList);
            if (!insertResult) {
                throw new ValidationException("货架新增失败");
            }
        }

        if (!updateList.isEmpty()) {
            boolean updateResult = shelfService.updateBatchById(updateList);
            if (!updateResult) {
                throw new ValidationException("货架更新失败");
            }
        }

        if (!deletedList.isEmpty()) {
            boolean deleteResult = shelfService.updateDeletedByIds(deletedList,req.getTenantId(), req.getUserId());
            if (!deleteResult) {
                throw new ValidationException("货架删除失败");
            }
        }

        return true;
    }

    public Page<ShelfPageListResp> pageList(Page<Shelives> page, ShelfListPageReq req) {
        Page<Shelives> list = shelivesService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<Long> whIds = list.getRecords().stream().map(v -> v.getWarehouseId()).collect(Collectors.toList());
        List<Warehouse> byIds = wareHouseService.getByIds(whIds, req.getTenantId());
        Map<Long, Warehouse> whMap = byIds.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));

        List<Long> shelivesIds = list.getRecords().stream().map(v -> v.getId()).collect(Collectors.toList());
        List<ShelfZone> zones = shelfService.selectByParentIds(req.getTenantId(), shelivesIds);
        Map<Long, List<ShelfZone>> sheliveId2ShelfZoneMap = zones.stream().collect(Collectors.groupingBy(ShelfZone::getParentId));

        List<ShelfPageListResp> collect = list.getRecords().stream().map(v -> {
            ShelfPageListResp p = new ShelfPageListResp();
            BeanUtils.copyProperties(v, p);

            p.setWarehouseName(whMap.get(v.getWarehouseId()).getName());

            if (sheliveId2ShelfZoneMap.containsKey(v.getId()))  {
                List<ShelfZone> l = sheliveId2ShelfZoneMap.getOrDefault(v.getId(), Collections.emptyList());
                List<ShelfPageListResp.ShelfZoneVO> collect1 = l.stream().map(s -> {
                    ShelfPageListResp.ShelfZoneVO v1 = new ShelfPageListResp.ShelfZoneVO();
                    BeanUtils.copyProperties(s, v1);
                    return v1;
                }).collect(Collectors.toList());
                p.setZones(collect1);
            }
            return p;
        }).collect(Collectors.toList());

        Page<ShelfPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }



    public Boolean update(ShelfCreateReq req) {
        ShelfZone wh = shelfService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("仓库不存在");
        }
        List<ShelfZone> list = shelfService.selectByCodeOrName(req.getShelfCode(), req.getShelfName(), req.getTenantId());
        if (!CollectionUtils.isEmpty(list)) {
            for (ShelfZone shelfZone : list) {
                if (!shelfZone.getId().equals(req.getId())) {
                    throw new ValidationException("仓库编号和名称已存在");
                }
            }
        }
        if (req.getCapacity() <= 0) {
            throw new ValidationException("货架容量不能小于0");
        }
        ShelfZone save = new ShelfZone();
        BeanUtils.copyProperties(req, save);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());

        return shelfService.updateById(save);
    }


    public Boolean updateStatus(ShelfUpdateStatusReq req) {
        Shelives shelives = shelivesService.selectById(req.getId(), req.getTenantId());
        if (shelives == null) {
            throw new ValidationException("货架不存在");
        }
        List<ShelfZone> wh = shelfService.selectByParentId(req.getTenantId(), shelives.getId());
        if (wh == null) {
            throw new ValidationException("货架区域不存在");
        }
        Shelives saveShelives = new Shelives();
        saveShelives.setId(req.getId());
        saveShelives.setStatus(req.getStatus());
        saveShelives.setModifiedAt(new Date());
        saveShelives.setModifiedBy(req.getUserId());
        Boolean s = shelivesService.updateById(saveShelives);
        if (!s) {
            throw new ValidationException("货架状态更新失败");
        }


        List<ShelfZone> batchUpdateList = new ArrayList<>();
        for (ShelfZone sz : wh) {
            ShelfZone save = new ShelfZone();
            save.setId(sz.getId());
            save.setStatus(req.getStatus());
            save.setModifiedAt(new Date());
            save.setModifiedBy(req.getUserId());
            batchUpdateList.add(save);
        }
        return shelfService.updateBatchById(batchUpdateList);
    }

    public List<ShelfPageListResp> listEnable(Long tenantId, Long warehouseId) {
        List<ShelfZone> list = shelfService.listWareHouseEnable(tenantId,warehouseId);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        //获取货架剩余容量
        List<Long> shelfIds  = list.stream().map(v -> v.getId()).collect(Collectors.toList());
        Map<Long, BigDecimal> shelfId2AcailableCapacityMap = getShelfUsedCapacity(tenantId, shelfIds);

        //查询顶层货架的名称
        List<Long> parentIds = list.stream().map(v -> v.getParentId()).collect(Collectors.toList());
        List<Shelives> parentShelves = shelivesService.selectByIds(tenantId, parentIds);
        Map<Long, String> parentId2NameMap = parentShelves.stream().collect(Collectors.toMap(Shelives::getId, v -> v.getShelfName()));

        return list.stream().map(v -> {
            ShelfPageListResp p = new ShelfPageListResp();
            BeanUtils.copyProperties(v, p);
            if (shelfId2AcailableCapacityMap.containsKey(v.getId())) {
                BigDecimal usedCapacity = shelfId2AcailableCapacityMap.getOrDefault(v.getId(), BigDecimal.ZERO);
                BigDecimal availableCapacity = v.getCapacity().subtract(usedCapacity);
                p.setAvailableCapacity(availableCapacity);
            } else {
                p.setAvailableCapacity(v.getCapacity());
            }
            if (parentId2NameMap.containsKey(v.getParentId())) {
                p.setSheliveName(parentId2NameMap.getOrDefault(v.getParentId(), ""));
            }
            //如果剩余容量小于0
            if (p.getAvailableCapacity().compareTo(BigDecimal.ZERO) <= 0) {
                p.setAvailableCapacity(BigDecimal.ZERO);
            }
            return p;
        }).collect(Collectors.toList());
    }

    public List<ShelfZoneResp> shelfZoneList(Long tenantId, Long parentId) {
        List<ShelfZone> shelfZones = shelfService.selectByParentId(tenantId, parentId);
        if (shelfZones.isEmpty()) {
            return new ArrayList<>();
        }
        return shelfZones.stream().map(v -> {
            ShelfZoneResp p = new ShelfZoneResp();
            BeanUtils.copyProperties(v, p);
            p.setZoneCode(v.getShelfCode());
            p.setZoneName(v.getShelfName());
            return p;
        }).collect(Collectors.toList());
    }
}
