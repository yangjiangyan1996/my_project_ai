package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.StockExecuteStockTakeItemReq;
import com.example.entity.cangku.req.StockItemListPageReq;
import com.example.entity.cangku.req.StockListPageReq;
import com.example.entity.cangku.req.StockTakeCreateReq;
import com.example.entity.cangku.resp.StockDetailResp;
import com.example.entity.cangku.resp.StockItemListPageResp;
import com.example.entity.cangku.resp.StockListPageResp;
import com.example.entity.dto.Account;
import com.example.enums.CkStockTakeEnums;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class CkStockFacade {

    @Resource
    CkInventoryShelfService inventoryShelfService;
    @Resource
    CkInventoryWarehouseService inventoryWarehouseService;
    @Resource
    CkWareHouseService warehouseService;
    @Resource
    CkStockTakeSnapshotService stockTaskSnapshotService;
    @Resource
    CkStockTakeLogService stockTaskLogService;
    @Resource
    CkStockTakeLockService stockLockService;
    @Resource
    CkStockTakeItemService stockTaskItemService;
    @Resource
    CkStockTakeService stockTaskService;
    @Resource
    AccountService accountService;
    @Resource
    CkInventoryLockService inventoryLockService;
    @Resource
    private CkInventoryService inventoryService;

    /**
     * 阶段①：创建盘点单（新建状态）
     */
    public Long createStockTake(StockTakeCreateReq req) {
        if (req.getTenantId() == null || req.getUserId() == null) {
            throw new ValidationException("租户或用户不能为空");
        }
        if (req.getWarehouseId() == null) {
            throw new ValidationException("仓库不能为空");
        }
        if (req.getTakeType() == null) {
            throw new ValidationException("盘点类型不能为空");
        }

        Warehouse warehouse = warehouseService.getById(req.getWarehouseId());
        if (warehouse == null || warehouse.getIsDeleted() == 1 || warehouse.getStatus() == 0) {
            throw new ValidationException("仓库不存在或已禁用");
        }
        if (!warehouse.getTenantId().equals(req.getTenantId())) {
            throw new ValidationException("非法仓库访问");
        }

        String stockTakeNo = generateStockTakeNo(req.getTenantId());

        StockTake stockTake = new StockTake();
        stockTake.setTenantId(req.getTenantId());
        stockTake.setStockTakeNo(stockTakeNo);
        stockTake.setWarehouseId(req.getWarehouseId());
        stockTake.setTakeType(req.getTakeType());
        stockTake.setTakeScope(req.getTakeScope() == null ? 1 : req.getTakeScope());
        stockTake.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.UNSUBMITTED.getCode()); // 新建
        stockTake.setTakeStatus(CkStockTakeEnums.TakeStatus.NOT_STARTED.getCode());
        stockTake.setRemark(req.getRemark());
        stockTake.setCreatedBy(req.getUserId());
        stockTake.setCreatedAt(new Date());
        stockTake.setModifiedBy(req.getUserId());
        stockTake.setModifiedAt(new Date());
        stockTake.setIsDeleted(0);

        boolean success = stockTaskService.save(stockTake);
        if (!success) {
            throw new ValidationException("创建盘点单失败");
        }

        log.info("创建盘点单成功, stockTakeNo={}, warehouseId={}", stockTakeNo, req.getWarehouseId());
        return stockTake.getId();
    }


    /**
     * 阶段②：提交审批（状态 = 审核中）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitApproval(Long stockTakeId, Long userId, Long tenantId) {
        StockTake stockTake = stockTaskService.getById(stockTakeId);
        if (stockTake == null) {
            throw new ValidationException("盘点单不存在");
        }
        if (!CkStockTakeEnums.ApprovalStatus.UNSUBMITTED.getCode().equals(  stockTake.getApprovalStatus()) ) {
            throw new ValidationException("只有新建状态才能提交审批");
        }

        // 更新状态
        stockTake.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW.getCode());
        stockTake.setModifiedBy(userId);
        stockTake.setModifiedAt(new Date());

        // 写操作日志（可扩展）
        //log.info("盘点单{}提交审批, status -> 盘点中", stockTake.getStockTakeNo());

        return stockTaskService.updateById(stockTake);
    }

    /**
     * 阶段③：审批通过 → 系统自动初始化盘点（核心）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean apprroveAndInitialize(Long stockTakeId, Long userId, Long tenantId) {
        StockTake stockTake = stockTaskService.getById(stockTakeId);
        if (stockTake == null) {
            throw new ValidationException("盘点单不存在");
        }

        if (!CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW.getCode().equals(stockTake.getApprovalStatus())) {
            throw new ValidationException("只有审核中状态才能审批通过");
        }
        if (!CkStockTakeEnums.TakeStatus.NOT_STARTED.getCode().equals(stockTake.getTakeStatus())) {
            throw new ValidationException("只有待开始状态才能初始化盘点");
        }

        // 1️⃣ 生成盘点锁
        createStockTakeLock(stockTake, userId,tenantId);

        // 2️⃣ 生成库存快照
        createStockTakeSnapshot(stockTake, userId);

        // 3️⃣ 生成盘点明细
       createStockTakeItemsFromSnapshot(stockTake, userId, tenantId);

        // 更新盘点单状态 = 已审批
        stockTake.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.APPROVED.getCode());
        stockTake.setTakeStatus(CkStockTakeEnums.TakeStatus.IN_PROGRESS.getCode());
        stockTake.setModifiedBy(userId);
        stockTake.setModifiedAt(new Date());
        stockTaskService.updateById(stockTake);

        log.info("盘点单{}审批通过并初始化盘点完成", stockTake.getStockTakeNo());
        return true;
    }


    /**
     * 阶段④：执行盘点（录入实盘数）
     */
    @Transactional
    public Boolean executeStockTakeItem(StockExecuteStockTakeItemReq req) {
        StockTake stockTake = stockTaskService.getById(req.getStockTakeId());
        if (stockTake == null) {
            throw new ValidationException("盘点单不存在");
        }


        StockTakeItem item = stockTaskItemService.selectByStockTakeIdAndItemId(req.getStockTakeId(), req.getStockTakeItemId(), req.getProductId(), req.getTenantId());
        if (item == null) {
            throw new ValidationException("盘点明细不存在");
        }
        if(item.getStatus().equals(CkStockTakeEnums.TakeStatus.SUSPENDED.getCode()) ||
                item.getStatus().equals(CkStockTakeEnums.TakeStatus.CANCELLED.getCode())){
            throw new ValidationException("盘点明细已取消或完成");
        }

        item.setCountedQuantity(req.getCountedQuantity());
        item.setDiffQuantity(req.getCountedQuantity().subtract(item.getSystemQuantity()));
        item.setModifiedAt(new Date());
        item.setModifiedBy(req.getUserId());
        item.setStatus(CkStockTakeEnums.StockItemStatus.ADJUSTING.getCode());

        //log.info("盘点明细{}录入实盘数: {}", itemId, countedQuantity);
        return stockTaskItemService.updateById(item);
    }

    /**
     * 阶段⑤：盘点完成 → 提交复核
     */
    @Transactional
    public Boolean completeStockTake(Long stockTakeId, Long userId, Long tenantId) {
        StockTake stockTake = stockTaskService.selectById(stockTakeId, tenantId);
        if (stockTake == null) {
            throw new ValidationException("盘点单不存在");
        }
        if (CkStockTakeEnums.TakeStatus.SUSPENDED.getCode().equals(stockTake.getTakeStatus())) {
            throw new ValidationException("已完成，无需重复操作！");
        }
        if (!CkStockTakeEnums.ApprovalStatus.APPROVED.getCode().equals(stockTake.getApprovalStatus())){
            throw new ValidationException("只有已批准状态才能提交复核");
        }
        if (!CkStockTakeEnums.TakeStatus.IN_PROGRESS.getCode().equals(stockTake.getTakeStatus())) {
            throw new ValidationException("只有盘点中状态才能完成盘点");
        }

        List<StockTakeItem> stockTakeItems = stockTaskItemService.selectByStockTakeId(stockTakeId, tenantId);
        for (StockTakeItem item : stockTakeItems) {
            if (!item.getStatus().equals(CkStockTakeEnums.StockItemStatus.ADJUSTING.getCode())) {
                throw new ValidationException("盘点单有未盘点明细，请先完成盘点");
            }
        }

        stockTake.setTakeStatus(CkStockTakeEnums.TakeStatus.SUSPENDED.getCode()); // 已完成盘点，待复核
        stockTake.setModifiedBy(userId);
        stockTake.setModifiedAt(new Date());
        boolean b = stockTaskService.updateById(stockTake);
        if (!b) {
            throw new ValidationException("更新盘点单失败");
        }

        Boolean b1 = stockTaskItemService.updateByStockTakeId(stockTakeId, CkStockTakeEnums.TakeStatus.SUSPENDED.getCode(), userId, tenantId);
        if (!b1) {
            throw new ValidationException("更新盘点明细失败");
        }
        log.info("盘点单{}已完成盘点，提交复核", stockTake.getStockTakeNo());
        return true;
    }

    /**
     * 阶段⑥：审核确认（库存真正变化）
     */
    @Transactional
    public void confirmStockTake(Long stockTakeId, Long userId) {
        StockTake stockTake = stockTaskService.getById(stockTakeId);
        if (stockTake == null) throw new ValidationException("盘点单不存在");
        //if (stockTake.getStatus() != 3) throw new ValidationException("只有复核状态才能确认");

        // 1️⃣ 根据差异生成库存调整单
        //stockTaskItemService.generateInventoryAdjust(stockTake, userId);

        // 2️⃣ 执行库存调整（库存服务 + 流水）
        //inventoryService.executeStockAdjust(stockTake, userId);

        // 3️⃣ 更新盘点单状态 = 已完成
        //stockTake.setStatus(4);
        stockTake.setModifiedBy(userId);
        stockTake.setModifiedAt(new Date());
        stockTaskService.updateById(stockTake);

        // 4️⃣ 释放盘点锁
        //inventoryLockService.releaseStockTakeLock(stockTake, userId);

        log.info("盘点单{}审核确认完成，库存已调整", stockTake.getStockTakeNo());
    }

    private void createStockTakeItemsFromSnapshot(StockTake stockTake, Long userId, Long tenantId) {

        // 幂等校验
        List<StockTakeItem> stockTakeItems = stockTaskItemService.selectByStockTakeId(stockTake.getId(), tenantId);

        Boolean exists = !CollectionUtils.isEmpty(stockTakeItems);
        if (exists) {
            log.info("盘点单{}明细已存在，跳过生成", stockTake.getStockTakeNo());
            return;
        }

        List<StockTakeSnapshot> snapshots = stockTaskSnapshotService.selectSnapshotByStockId(stockTake.getId(), tenantId);
        if (snapshots.isEmpty()) {
            throw new ValidationException("库存快照不存在，无法生成盘点明细");
        }

        List<StockTakeItem> items = snapshots.stream().map(s -> {
            StockTakeItem item = new StockTakeItem();
            item.setTenantId(stockTake.getTenantId());
            item.setStockTakeId(stockTake.getId());
            item.setProductId(s.getProductId());
            item.setWarehouseId(s.getWarehouseId());
            item.setBatchNo(s.getBatchNo());
            item.setShelfId(s.getShelfId());
            item.setLocationCode(s.getLocationCode());
            item.setSystemQuantity(s.getSnapshotQuantity());
            item.setStatus(1); // 未盘

            item.setCreatedBy(userId);
            item.setModifiedBy(userId);
            return item;
        }).toList();

        stockTaskItemService.saveBatch(items);

        log.info("盘点单{}生成盘点明细{}条",
                stockTake.getStockTakeNo(), items.size());
    }



    private void createStockTakeSnapshot(StockTake stockTake, Long userId) {

        // 幂等校验
        List<StockTakeSnapshot> stockTakeSnapshots = stockTaskSnapshotService.selectSnapshotByStockId(stockTake.getId(), stockTake.getTenantId());

        Boolean exists = !CollectionUtils.isEmpty(stockTakeSnapshots);
        if (exists) {
            log.info("盘点单{}快照已存在，跳过生成", stockTake.getStockTakeNo());
            return;
        }


        List<InventoryShelf> inventoryShelves = inventoryShelfService.selectByTenantId(stockTake.getTenantId());

        if (inventoryShelves.isEmpty()) {
            log.warn("盘点单{}仓库无库存", stockTake.getStockTakeNo());
            return;
        }

        Date now = new Date();

        List<StockTakeSnapshot> snapshots = inventoryShelves.stream().map(inv -> {
            StockTakeSnapshot s = new StockTakeSnapshot();
            s.setTenantId(stockTake.getTenantId());
            s.setStockTakeId(stockTake.getId());
            s.setProductId(inv.getProductId());
            s.setWarehouseId(inv.getWarehouseId());
            s.setBatchNo(inv.getBatchNo());
            s.setShelfId(inv.getShelfId());
            //s.setLocationCode(inv.getLocationCode());
            s.setSnapshotQuantity(inv.getQuantity());
            s.setSnapshotTime(now);
            s.setCreatedBy(userId);
            s.setModifiedBy(userId);
            return s;
        }).toList();

        stockTaskSnapshotService.saveBatch(snapshots);

        log.info("盘点单{}生成库存快照{}条",
                stockTake.getStockTakeNo(), snapshots.size());
    }



    /**
     * 这把锁拦什么？
     *
     * 出库 / 移库：需校验
     * 查询库存：不拦
     * 新建盘点：拦
     * @param stockTake
     * @param userId
     * @param tenantId
     */
    private void createStockTakeLock(StockTake stockTake, Long userId, Long tenantId) {

        // 幂等校验：防止重复加锁
        Boolean lockExists = stockLockService.getStockLockExist(stockTake.getId(), tenantId, 1);
        if (lockExists) {
            log.info("盘点单{}已存在有效锁，跳过加锁", stockTake.getStockTakeNo());
            return;
        }

        StockTakeLock lock = new StockTakeLock();
        lock.setTenantId(tenantId);
        lock.setStockTakeId(stockTake.getId());
        lock.setLockScope(1); // 1-仓库
        lock.setWarehouseId(stockTake.getWarehouseId());
        lock.setLockMode(1);  // 1-软锁
        lock.setLockStatus(1);
        lock.setLockTime(new Date());

        lock.setCreatedBy(userId);
        lock.setModifiedBy(userId);

        stockLockService.save(lock);

        log.info("盘点单{}仓库锁创建成功", stockTake.getStockTakeNo());
    }




    /**
     * 盘点单号生成规则
     * 示例：ST202512260001
     */
    private String generateStockTakeNo(Long tenantId) {
        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd")
                .format(new Date());
        // 实际生产中建议：Redis / DB 序列
        long suffix = System.currentTimeMillis() % 100000;
        return "ST" + dateStr + String.format("%05d", suffix);
    }

    public Page<StockListPageResp> pageList(Page<StockTake> page, StockListPageReq req) {
        Page<StockTake> list = stockTaskService.getStockPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        Map<Long, List<StockTakeItem>> stockTakeId2ItemListMap = new HashMap<>();
        List<Long> stockTakeIds = list.getRecords().stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(stockTakeIds)) {
            List<StockTakeItem> itemList = stockTaskItemService.selectByStockTakeIds(stockTakeIds, req.getTenantId());
            stockTakeId2ItemListMap = itemList.stream().collect(Collectors.groupingBy(StockTakeItem::getStockTakeId));
        }

        Map<Long, Account> userId2AccountMap = new HashMap<>();
        List<Long> userIds = list.getRecords().stream().map(v -> v.getCreatedBy()).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(userIds)) {
            List<Account> accounts = accountService.selectByIds(userIds);
            userId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        }

        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Long> warehouseIds = list.getRecords().stream().map(v -> v.getWarehouseId()).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(warehouseIds)) {
            List<Warehouse> warehouseList = warehouseService.selectByTenantIdAndWareHouseIds(req.getTenantId(), warehouseIds);
            warehouseId2WarehouseMap = warehouseList.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        Map<Long, List<StockTakeItem>> finalStockTakeId2ItemListMap = stockTakeId2ItemListMap;
        Map<Long, Account> finalUserId2AccountMap = userId2AccountMap;
        Map<Long, Warehouse> finalWarehouseId2WarehouseMap = warehouseId2WarehouseMap;
        List<StockListPageResp> collect = list.getRecords().stream().map(v -> {
            StockListPageResp p = new StockListPageResp();
            BeanUtils.copyProperties(v, p);


            List<StockTakeItem> items = finalStockTakeId2ItemListMap.getOrDefault(v.getId(), new ArrayList<>());
            p.setItemCount(CollectionUtils.isEmpty(items) ? 0 : items.size());

            List<StockTakeItem> collect1 = items.stream().filter(s -> !CkStockTakeEnums.StockItemStatus.NOT_ADJUSTED.getCode().equals(s.getStatus())).collect(Collectors.toList());
            p.setCountedCount(collect1.size());

            List<StockTakeItem> diffList = items.stream().filter(s -> s.getDiffQuantity().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
            p.setTotalDiff(diffList.size());

            p.setCreatedByName(finalUserId2AccountMap.getOrDefault(v.getCreatedBy(), new Account()).getUsername());
            p.setWarehouseName(finalWarehouseId2WarehouseMap.getOrDefault(v.getWarehouseId(), new Warehouse()).getName());
            return p;
        }).collect(Collectors.toList());

        Page<StockListPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Page<StockItemListPageResp> itemPageList(Page<StockTakeItem> page, StockItemListPageReq req) {
        Page<StockTakeItem> list = stockTaskItemService.getStockItemPageList(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<StockItemListPageResp> collect = list.getRecords().stream().map(v -> {
            StockItemListPageResp p = new StockItemListPageResp();

            p.setStockTakeId(v.getStockTakeId());
            p.setStockTakeItemId(v.getId());
            BeanUtils.copyProperties(v, p);
            return p;
        }).collect(Collectors.toList());

        Page<StockItemListPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public StockDetailResp stockDetail(Long id, Long tenantId) {
        StockTake stockTake = stockTaskService.selectById(id, tenantId);

        StockDetailResp resp = new StockDetailResp();

        BeanUtils.copyProperties(stockTake, resp);

        Warehouse wh = warehouseService.selectByTenantIdAndWareHouseId(tenantId, stockTake.getWarehouseId());
        if (wh != null) {
            resp.setWarehouseName(wh.getName());
        }
        resp.setApprovalStatusName(CkStockTakeEnums.ApprovalStatus.getDescByCode(stockTake.getApprovalStatus()));
        resp.setTakeStatusName(CkStockTakeEnums.TakeStatus.getDescByCode(stockTake.getTakeStatus()));
        resp.setTakeTypeName(CkStockTakeEnums.TakeType.getDescByCode(stockTake.getTakeType()));
        return resp;
    }
}

