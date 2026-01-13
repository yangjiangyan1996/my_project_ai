package com.example.Facade;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.StockDetailResp;
import com.example.entity.cangku.resp.StockItemListPageResp;
import com.example.entity.cangku.resp.StockListPageResp;
import com.example.entity.dto.Account;
import com.example.entity.resp.StockSnapResp;
import com.example.enums.CkStockTakeEnums;
import com.example.service.*;
import com.google.common.collect.Lists;
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
    CkProductService productService;
    @Resource
    CkShelfZoneService shelfService;
    @Resource
    CkInventoryShelfService inventoryShelfService;
    @Resource
    CkInventoryWarehouseService inventoryWarehouseService;
    @Resource
    CkWareHouseService warehouseService;
    @Resource
    CkStockTakeService stockTakeService;
    @Resource
    CkStockTakeTaskService stockTakeTaskService;
    @Resource
    CkStockTakeSnapshotService stockTaskSnapshotService;
    @Resource
    CkStockTakeLogService stockTaskLogService;
    @Resource
    CkStockTakeLockService stockLockService;
    @Resource
    CkStockTakeItemService stockTaskItemService;
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
        stockTake.setTakeScope(req.getTakeScope());
        if (req.getTakeScope() != null) {
            if (req.getTakeScope() == 2) {
                if (CollectionUtils.isEmpty(req.getBatchIds())) {
                    throw new ValidationException("请选择批次");
                }
                stockTake.setTaskScopeValue(JSON.toJSONString(req.getBatchIds()));
            } else if (req.getTakeScope() == 3) {
                if (CollectionUtils.isEmpty(req.getShelfIds())) {
                    throw new ValidationException("请选择货架");
                }
                stockTake.setTaskScopeValue(JSON.toJSONString(req.getShelfIds()));
            } else if (req.getTakeScope() == 4) {
                if (CollectionUtils.isEmpty(req.getProductIds())) {
                    throw new ValidationException("请选择商品");
                }
            }
        }
        stockTake.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.UNSUBMITTED.getCode()); // 新建
        stockTake.setTakeStatus(CkStockTakeEnums.TakeStatus.NOT_STARTED.getCode());
        stockTake.setRemark(req.getRemark());
        stockTake.setCreatedBy(req.getUserId());
        stockTake.setCreatedAt(new Date());
        stockTake.setModifiedBy(req.getUserId());
        stockTake.setModifiedAt(new Date());
        stockTake.setIsDeleted(0);

        boolean success = stockTakeService.save(stockTake);
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
        StockTake stockTake = stockTakeService.getById(stockTakeId);
        if (stockTake == null) {
            throw new ValidationException("盘点单不存在");
        }
        if (!CkStockTakeEnums.ApprovalStatus.UNSUBMITTED.getCode().equals(stockTake.getApprovalStatus())) {
            throw new ValidationException("只有新建状态才能提交审批");
        }

        // 更新状态
        stockTake.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW.getCode());
        stockTake.setModifiedBy(userId);
        stockTake.setModifiedAt(new Date());

        // 写操作日志（可扩展）
        //log.info("盘点单{}提交审批, status -> 盘点中", stockTake.getStockTakeNo());

        return stockTakeService.updateById(stockTake);
    }

    /**
     * 阶段③：审批通过 → 系统自动初始化盘点（核心）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean apprroveAndInitialize(Long stockTakeId, Long userId, Long tenantId) {
        StockTake stockTake = stockTakeService.getById(stockTakeId);
        if (stockTake == null) {
            throw new ValidationException("盘点单不存在");
        }

        if (!CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW.getCode().equals(stockTake.getApprovalStatus())) {
            throw new ValidationException("只有审核中状态才能审批通过");
        }
        if (!CkStockTakeEnums.TakeStatus.NOT_STARTED.getCode().equals(stockTake.getTakeStatus())) {
            throw new ValidationException("只有待开始状态才能初始化盘点");
        }

        //如果审核拒绝
        //1、修改状态
//        if (approveStatus approveStatus.equals(CkStockTakeEnums.ApprovalStatus.REJECTED.getCode())) {
//            stockTake.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.REJECTED.getCode());
//            stockTake.setModifiedBy(userId);
//            stockTake.setModifiedAt(new Date());
//            stockTaskService.updateById(stockTake);
//            //异常操作流程
//            return true;
//        }

        //如果审批通过
        // 1️⃣ 生成盘点锁
        createStockTakeLock(stockTake, userId, tenantId);

        // 2️⃣ 生成库存快照
        createStockTakeSnapshot(stockTake, userId);

        // 3️⃣ 生成盘点明细
        createStockTakeItemsFromSnapshot(stockTake, userId, tenantId);

        // 4️⃣ 如果盘点单是静态盘点，需要锁定库存
        if (CkStockTakeEnums.TakeType.STATIC_TAKE.getCode().equals(stockTake.getTakeType())) {
            lockInventoryForStock(stockTake, userId, tenantId);
        }

        // 更新盘点单状态 = 已审批
        stockTake.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.APPROVED.getCode());
        stockTake.setTakeStatus(CkStockTakeEnums.TakeStatus.IN_PROGRESS.getCode());
        stockTake.setModifiedBy(userId);
        stockTake.setModifiedAt(new Date());
        stockTakeService.updateById(stockTake);

        log.info("盘点单{}审批通过并初始化盘点完成", stockTake.getStockTakeNo());
        return true;
    }

    private void lockInventoryForStock(StockTake stockTake, Long userId, Long tenantId) {
        //锁定库存
        //考虑在出库的时候，InventoryHolder类中判断有没有锁，有锁就报错
    }


    /**
     * 阶段④：执行盘点（录入实盘数）
     */
    @Transactional
    public Boolean executeStockTakeItem(StockExecuteStockTakeItemReq req) {
        StockTake stockTake = stockTakeService.getById(req.getStockTakeId());
        if (stockTake == null) {
            throw new ValidationException("盘点单不存在");
        }


        StockTakeItem item = stockTaskItemService.selectByStockTakeIdAndItemId(req.getStockTakeId(), req.getStockTakeItemId(), req.getProductId(), req.getTenantId());
        if (item == null) {
            throw new ValidationException("盘点明细不存在");
        }
        if (item.getStatus().equals(CkStockTakeEnums.TakeStatus.SUSPENDED.getCode()) ||
                item.getStatus().equals(CkStockTakeEnums.TakeStatus.CANCELLED.getCode())) {
            throw new ValidationException("盘点明细已取消或完成");
        }

        item.setCountedQuantity(req.getCountedQuantity());
        item.setDiffQuantity(req.getCountedQuantity().subtract(item.getSystemQuantity()));
        item.setModifiedAt(new Date());
        item.setModifiedBy(req.getUserId());
        item.setStatus(CkStockTakeEnums.StockItemStatus.ADJUSTING.getCode());

        //log.info("盘点明细{}录入实盘数: {}", itemId, countedQuantity);
        boolean updateResult = stockTaskItemService.updateById(item);


        //更新实际开始时间
        List<StockTakeTask> stockTakeTasks = stockTakeTaskService.selectByStockTakeIdAndExecutorId(req.getTenantId(), req.getStockTakeId(), req.getUserId());
        for (StockTakeTask stt : stockTakeTasks) {
            if (stt.getActualStartTime() == null) {
                //设置开始时间
                String locationRange = stt.getLocationRange();
                List<Long> stockItemIds = JSON.parseArray(locationRange, Long.class);
                if (stockItemIds.contains(item.getId())) {
                    stt.setActualStartTime(new Date());
                    stockTakeTaskService.updateById(stt);
                }
            }
        }
        return true;
    }

    /**
     * 阶段⑤：盘点完成 → 提交复核
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean completeStockTake(Long stockTakeId, Long userId, Long tenantId) {
        StockTake stockTake = stockTakeService.selectById(stockTakeId, tenantId);
        if (stockTake == null) {
            throw new ValidationException("盘点单不存在");
        }
        if (CkStockTakeEnums.TakeStatus.SUSPENDED.getCode().equals(stockTake.getTakeStatus())) {
            throw new ValidationException("已完成，无需重复操作！");
        }
        if (!CkStockTakeEnums.ApprovalStatus.APPROVED.getCode().equals(stockTake.getApprovalStatus())) {
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
        boolean b = stockTakeService.updateById(stockTake);
        if (!b) {
            throw new ValidationException("更新盘点单失败");
        }

        Boolean b1 = stockTaskItemService.updateByStockTakeId(stockTakeId, CkStockTakeEnums.TakeStatus.SUSPENDED.getCode(), userId, tenantId);
        if (!b1) {
            throw new ValidationException("更新盘点明细失败");
        }

        //删除锁
        Boolean delectLockResult = stockLockService.releaseByStockTakeId(stockTakeId, userId, tenantId);
        if (!delectLockResult) {
            throw new ValidationException("释放锁失败");
        }
        log.info("盘点单{}已完成盘点，提交复核", stockTake.getStockTakeNo());
        return true;
    }

    /**
     * 阶段⑥：审核确认（库存真正变化）
     */
    @Transactional
    public void stockCreateAdjustmentOrder(Long stockTakeId, Long userId, Long tenantId) {
        StockTake stockTake = stockTakeService.getById(stockTakeId);
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
        stockTakeService.updateById(stockTake);

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

        Integer takeScope = stockTake.getTakeScope();
        CkStockTakeEnums.StockTakeScope scopeEnum = CkStockTakeEnums.StockTakeScope.getByCode(takeScope);
        if (scopeEnum == null) {
            throw new ValidationException("盘点单范围错误");
        }
        List<InventoryShelf> inventoryShelves = null;
        switch (scopeEnum) {
            case ALL:
                //全部仓库
                inventoryShelves = inventoryShelfService.selectByTenantId(stockTake.getTenantId());
                break;
            case BATCH:
                //仓库的批次
                String taskScopeValue = stockTake.getTaskScopeValue();
                List<String> batchNos = JSON.parseArray(taskScopeValue, String.class);
                inventoryShelves = inventoryShelfService.selectByWarehouseIdAndBatchNos(stockTake.getWarehouseId(), stockTake.getTenantId(), batchNos);
                break;
            case SHELF:
                //仓库的货架
                String taskScopeValue1 = stockTake.getTaskScopeValue();
                List<Long> shelfIds = JSON.parseArray(taskScopeValue1, Long.class);
                inventoryShelves = inventoryShelfService.selectByWarehouseIdAndShelfIds(stockTake.getWarehouseId(), stockTake.getTenantId(), shelfIds);
                break;
            case PRODUCT:
                //仓库的商品
                String taskScopeValue2 = stockTake.getTaskScopeValue();
                List<Long> productIds = JSON.parseArray(taskScopeValue2, Long.class);
                inventoryShelves = inventoryShelfService.selectByWarehouseIdAndProductIds(stockTake.getWarehouseId(), stockTake.getTenantId(), productIds);
                break;
            default:
                throw new ValidationException("盘点范围错误");

        }

        if (inventoryShelves.isEmpty()) {
            log.warn("盘点单{}仓库无库存", stockTake.getStockTakeNo());
            return;
        }
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
            s.setSnapshotTime(new Date());
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
     * <p>
     * 出库 / 移库：需校验
     * 查询库存：不拦
     * 新建盘点：拦
     *
     * @param stockTake
     * @param userId
     * @param tenantId
     */
    private void createStockTakeLock(StockTake stockTake, Long userId, Long tenantId) {
        //防止重复盘点
        Boolean lockExist = stockLockService.getLockExist(stockTake.getTakeScope(), stockTake.getWarehouseId(), stockTake.getTenantId(), 1);
        if (lockExist) {
            throw new ValidationException("已存在有效盘点单");
        }

        // 幂等校验：防止重复加锁
        Boolean stocklockExists = stockLockService.getStockLockExist(stockTake.getId(), tenantId, 1);
        if (stocklockExists) {
            log.info("盘点单{}已存在有效锁，跳过加锁", stockTake.getStockTakeNo());
            throw new ValidationException("盘点单已存在有效锁");
        }

        StockTakeLock lock = new StockTakeLock();
        lock.setTenantId(tenantId);
        lock.setStockTakeId(stockTake.getId());
        lock.setLockScope(stockTake.getTakeScope()); // 1-仓库
        lock.setWarehouseId(stockTake.getWarehouseId());
        lock.setLockMode(stockTake.getTakeType());  // 1-软锁
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
        Page<StockTake> list = stockTakeService.getStockPage(page, req);
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

            List<StockTakeItem> diffList = items.stream().filter(s -> s.getDiffQuantity() != null && s.getDiffQuantity().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
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


        // 获取盘点单任务权限
        List<Long> hasPermissionItemIds = new ArrayList<>();
        List<StockTakeTask> tasks =stockTakeTaskService.selectByStockTakeIdAndExecutorId( req.getTenantId(), req.getStockTakeId(),req.getUserId());
        for (StockTakeTask task : tasks) {
            String locationRange = task.getLocationRange();
            if (locationRange != null && !locationRange.isEmpty()) {
                List<Long> stockItemIds = JSON.parseArray(locationRange, Long.class);
                hasPermissionItemIds.addAll(stockItemIds);
            }
        }

        //获取产品信息
        List<Long> productIds = list.getRecords().stream().map(StockTakeItem::getProductId).distinct().collect(Collectors.toList());
        List<Product> products = productService.selectByIds(req.getTenantId(), productIds);
        Map<Long, Product> productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));

        List<Long> shelfIds = list.getRecords().stream().map(StockTakeItem::getShelfId).distinct().collect(Collectors.toList());
        List<ShelfZone> warehouseShelves = shelfService.selectByIds(shelfIds, req.getTenantId());
        Map<Long, ShelfZone> shelfId2ShelfMap = warehouseShelves.stream().collect(Collectors.toMap(ShelfZone::getId, v -> v));

        List<StockItemListPageResp> collect = list.getRecords().stream().map(v -> {
            StockItemListPageResp p = new StockItemListPageResp();
            BeanUtils.copyProperties(v, p);

            p.setStockTakeId(v.getStockTakeId());
            p.setStockTakeItemId(v.getId());
            if (hasPermissionItemIds.contains(v.getId())) {
                p.setHasStockItemTaskPermission(true);
            } else {
                p.setHasStockItemTaskPermission(false);
            }

            if (productId2ProductMap.containsKey(v.getProductId())) {
                Product product = productId2ProductMap.get(v.getProductId());
                p.setProductName(product.getName());
                p.setSpec(product.getSpec());
                p.setSku(product.getSku());
                p.setColor(product.getColor());
            }

            if (shelfId2ShelfMap.containsKey(v.getShelfId())) {
                ShelfZone warehouseShelf = shelfId2ShelfMap.get(v.getShelfId());
                p.setShelfName(warehouseShelf.getShelfName());
            }
            return p;
        }).collect(Collectors.toList());

        Page<StockItemListPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public List<StockItemListPageResp> itemList(StockItemListPageReq req) {
        List<StockTakeItem> list = stockTaskItemService.selectByStockTakeId(req.getStockTakeId(), req.getTenantId());
        if (CollectionUtils.isEmpty( list)) {
            return new ArrayList<>();
        }


        // 获取盘点单任务权限
        List<Long> hasPermissionItemIds = new ArrayList<>();
        List<StockTakeTask> tasks =stockTakeTaskService.selectByStockTakeIdAndExecutorId( req.getTenantId(), req.getStockTakeId(),req.getUserId());
        for (StockTakeTask task : tasks) {
            String locationRange = task.getLocationRange();
            if (locationRange != null && !locationRange.isEmpty()) {
                List<Long> stockItemIds = JSON.parseArray(locationRange, Long.class);
                hasPermissionItemIds.addAll(stockItemIds);
            }
        }

        //获取产品信息
        List<Long> productIds = list.stream().map(StockTakeItem::getProductId).distinct().collect(Collectors.toList());
        List<Product> products = productService.selectByIds(req.getTenantId(), productIds);
        Map<Long, Product> productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));

        List<Long> shelfIds = list.stream().map(StockTakeItem::getShelfId).distinct().collect(Collectors.toList());
        List<ShelfZone> warehouseShelves = shelfService.selectByIds(shelfIds, req.getTenantId());
        Map<Long, ShelfZone> shelfId2ShelfMap = warehouseShelves.stream().collect(Collectors.toMap(ShelfZone::getId, v -> v));

        return  list.stream().map(v -> {
            StockItemListPageResp p = new StockItemListPageResp();
            BeanUtils.copyProperties(v, p);

            p.setStockTakeId(v.getStockTakeId());
            p.setStockTakeItemId(v.getId());
            if (hasPermissionItemIds.contains(v.getId())) {
                p.setHasStockItemTaskPermission(true);
            } else {
                p.setHasStockItemTaskPermission(false);
            }

            if (productId2ProductMap.containsKey(v.getProductId())) {
                Product product = productId2ProductMap.get(v.getProductId());
                p.setProductName(product.getName());
                p.setSpec(product.getSpec());
                p.setSku(product.getSku());
                p.setColor(product.getColor());
            }

            if (shelfId2ShelfMap.containsKey(v.getShelfId())) {
                ShelfZone warehouseShelf = shelfId2ShelfMap.get(v.getShelfId());
                p.setShelfName(warehouseShelf.getShelfName());
            }
            return p;
        }).collect(Collectors.toList());

    }

    public StockDetailResp stockDetail(Long id, Long tenantId) {
        StockTake stockTake = stockTakeService.selectById(id, tenantId);

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

    public List<StockSnapResp> getListOfStockTakeToBeAssigned(Long stockTakeId, CkStockTakeEnums.StockTakeScope taskScopeEnum, Long tenantId) {
        List<StockTakeSnapshot> stockTakeSnapshots = stockTaskSnapshotService.selectSnapshotByStockId(stockTakeId, tenantId);
        if (CollectionUtils.isEmpty(stockTakeSnapshots)) {
            return new ArrayList<>();
        }

        List<StockSnapResp> list = new ArrayList<>();
        switch (taskScopeEnum) {
            case ALL:
                break;
            case BATCH:
                Map<String, List<StockTakeSnapshot>> batchNo2SnapListMap = stockTakeSnapshots.stream().collect(Collectors.groupingBy(StockTakeSnapshot::getBatchNo));
                for (String batNo : batchNo2SnapListMap.keySet()) {
                    StockSnapResp r = new StockSnapResp();
                    List<StockTakeSnapshot> stsList = batchNo2SnapListMap.getOrDefault(batNo, null);
                    //计算list中 BigDecimal snapshotQuantity 的总和
                    BigDecimal sum = stsList.stream().map(StockTakeSnapshot::getSnapshotQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                    r.setId(batNo);
                    r.setName(batNo);
                    r.setQuantity(sum);
                    list.add(r);
                }
                break;
            case SHELF:
                Map<Long, List<StockTakeSnapshot>> shelfId2SnapListMap = stockTakeSnapshots.stream().collect(Collectors.groupingBy(StockTakeSnapshot::getShelfId));
                List<ShelfZone> warehouseShelves = shelfService.selectByIds(Lists.newArrayList(shelfId2SnapListMap.keySet()), tenantId);
                Map<Long, ShelfZone> shelfId2ShelfMap = warehouseShelves.stream().collect(Collectors.toMap(ShelfZone::getId, v -> v));
                for (Long shelfId : shelfId2SnapListMap.keySet()) {
                    StockSnapResp r = new StockSnapResp();
                    List<StockTakeSnapshot> stsList = shelfId2SnapListMap.getOrDefault(shelfId, null);
                    //计算list中 BigDecimal snapshotQuantity 的总和
                    BigDecimal sum = stsList.stream().map(StockTakeSnapshot::getSnapshotQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                    r.setId(shelfId + "");
                    r.setName(shelfId2ShelfMap.getOrDefault(shelfId, new ShelfZone()).getShelfName());
                    r.setQuantity(sum);
                    list.add(r);
                }
                break;
            case PRODUCT:
                Map<Long, List<StockTakeSnapshot>> productId2SnapListMap = stockTakeSnapshots.stream().collect(Collectors.groupingBy(StockTakeSnapshot::getProductId));
                List<Product> productList = productService.selectByIds(tenantId, Lists.newArrayList(productId2SnapListMap.keySet()));
                Map<Long, Product> productId2ProductMap = productList.stream().collect(Collectors.toMap(Product::getId, v -> v));
                for (Long productId : productId2SnapListMap.keySet()) {
                    StockSnapResp r = new StockSnapResp();
                    List<StockTakeSnapshot> stsList = productId2SnapListMap.getOrDefault(productId, null);
                    //计算list中 BigDecimal snapshotQuantity 的总和
                    BigDecimal sum = stsList.stream().map(StockTakeSnapshot::getSnapshotQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (productId2ProductMap.containsKey(productId)) {
                        Product product = productId2ProductMap.get(productId);
                        r.setName(product.getName() + "-" + product.getSpec() + "-" + product.getColor());
                    }
                    r.setId(productId + "");
                    r.setQuantity(sum);
                    list.add(r);
                }
                break;
            default:
                throw new RuntimeException("暂不支持的库存盘点范围");
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean assignTask(StockAssignTaskReq req) {
        StockTake stockTake = stockTakeService.selectById(req.getStockTakeId(), req.getTenantId());
        if (stockTake == null) {
            throw new RuntimeException("盘点单不存在");
        }
        //删除
        Boolean deletedResult = stockTakeTaskService.deletedByStockId(req.getStockTakeId(), req.getTenantId(),req.getUserId());


        /**
         *  创建指派任务
         */
        final List<StockTakeItem> needAssignTask = stockTaskItemService.selectByStockTakeId(req.getStockTakeId(), req.getTenantId());
        Integer assignType = req.getAssignType();
        CkStockTakeEnums.AssignType atEnum = CkStockTakeEnums.AssignType.getByCode(assignType);
        if (atEnum == null) {
            throw new ValidationException("暂不支持的分配类型");
        }
        List<StockTakeTask> saveBatchList = new ArrayList<>();
        switch (atEnum) {
            case PERSON:
                //按人分配
                List<Long> itemIds = needAssignTask.stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
                for (Long assigneeId : req.getAssigneeIds()) {
                    StockTakeTask s = new StockTakeTask();
                    s.setTenantId(req.getTenantId());
                    s.setStockTakeId(req.getStockTakeId());
                    s.setTaskNo(stockTake.getStockTakeNo() + "-" + generateStockTakeNo(req.getTenantId()));
                    s.setAssignType(req.getAssignType());
                    s.setStockTakeNo(stockTake.getStockTakeNo());
                    s.setWarehouseId(stockTake.getWarehouseId());
                    s.setStockTakeType(stockTake.getTakeType());
                    s.setAssigneeId(req.getUserId());
                    s.setExecutorId(assigneeId);
                    s.setTaskStatus(CkStockTakeEnums.StockTakeTaskStatus.ASSIGNED.getCode());
                    s.setTotalItems(needAssignTask.size());
                    s.setCompletedItems(0);
                    s.setPlanStartTime(req.getPlanStartTime());
                    s.setPlanEndTime(req.getPlanEndTime());
                    s.setPriority(req.getPriority());
                    s.setRemark(req.getRemark());
                    s.setLocationRange(JSON.toJSONString(itemIds));
                    saveBatchList.add(s);
                }
                break;
            case CONDITION:
                //按条件分配
                Integer assignDimension = req.getAssignDimension();
                CkStockTakeEnums.AssignConditionType taskScopeEnum = CkStockTakeEnums.AssignConditionType.getByCode(assignDimension);
                if (taskScopeEnum == null) {
                    throw new ValidationException("暂不支持的库存盘点范围");
                }
                saveBatchList = getSaveBatchList(stockTake, req,needAssignTask);
                break;
            default:
                throw new ValidationException("暂不支持的分配类型");
        }
        boolean b = stockTakeTaskService.saveBatch(saveBatchList);
        if (!b) {
            throw new ValidationException("保存库存盘点任务失败");
        }

        /**
         *
         */
        return b;
    }

    private List<StockTakeTask> getSaveBatchList(StockTake stockTake, StockAssignTaskReq req, final List<StockTakeItem> needAssignTask) {
        List<StockTakeTask> saveBatchList = new ArrayList<>();
        Map<Long, List<String>> userId2BatchNo_shelfId_productId_ListMap = new HashMap<>();
        for (StockAssignTaskReq.AssignCondition item : req.getConditions()) {
            String dimensionValue = item.getDimensionValue();
            for (Long assigneeId : item.getAssigneeIds()) {
                if (userId2BatchNo_shelfId_productId_ListMap.containsKey(assigneeId)) {
                    List<String> batchNos = userId2BatchNo_shelfId_productId_ListMap.get(assigneeId);
                    batchNos.add(dimensionValue);
                    userId2BatchNo_shelfId_productId_ListMap.put(assigneeId, batchNos);
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(dimensionValue);
                    userId2BatchNo_shelfId_productId_ListMap.put(assigneeId, list);
                }
            }
        }
        Map<Long, List<Long>> userId2ItemIdsMap = new HashMap<>();
        if (req.getAssignDimension().equals(CkStockTakeEnums.AssignConditionType.BATCH.getCode())) {
            Map<String, List<Long>> batchNo2ItemIdsMap = needAssignTask.stream().collect(Collectors.groupingBy(StockTakeItem::getBatchNo, Collectors.mapping(StockTakeItem::getId, Collectors.toList())));
            for (Long userId : userId2BatchNo_shelfId_productId_ListMap.keySet()) {
                List<String> list = userId2BatchNo_shelfId_productId_ListMap.get(userId);
                List<Long> itemIds = new ArrayList<>();
                for (String batchNo_shelfId_productId : list) {
                    List<Long> longs = batchNo2ItemIdsMap.get(batchNo_shelfId_productId);
                    itemIds.addAll(longs);
                }
                userId2ItemIdsMap.put(userId, itemIds);
            }
        } else if (req.getAssignDimension().equals(CkStockTakeEnums.AssignConditionType.SHELF.getCode())) {
            Map<Long, List<Long>> shelfId2ItemIdsMap = needAssignTask.stream().collect(Collectors.groupingBy(StockTakeItem::getShelfId, Collectors.mapping(StockTakeItem::getId, Collectors.toList())));
            for (Long userId : userId2BatchNo_shelfId_productId_ListMap.keySet()) {
                List<String> list = userId2BatchNo_shelfId_productId_ListMap.get(userId);
                List<Long> itemIds = new ArrayList<>();
                for (String batchNo_shelfId_productId : list) {
                    List<Long> longs = shelfId2ItemIdsMap.get(Long.valueOf(batchNo_shelfId_productId));
                    itemIds.addAll(longs);
                }
                userId2ItemIdsMap.put(userId, itemIds);
            }
        } else if (req.getAssignDimension().equals(CkStockTakeEnums.AssignConditionType.PRODUCT.getCode())) {
            Map<Long, List<Long>> productId2ItemIdsMap = needAssignTask.stream().collect(Collectors.groupingBy(StockTakeItem::getProductId, Collectors.mapping(StockTakeItem::getId, Collectors.toList())));
            for (Long userId : userId2BatchNo_shelfId_productId_ListMap.keySet()) {
                List<String> list = userId2BatchNo_shelfId_productId_ListMap.get(userId);
                List<Long> itemIds = new ArrayList<>();
                for (String batchNo_shelfId_productId : list) {
                    List<Long> longs = productId2ItemIdsMap.get(Long.valueOf(batchNo_shelfId_productId));
                    itemIds.addAll(longs);
                }
                userId2ItemIdsMap.put(userId, itemIds);
            }
        }

        for (Long userId : userId2BatchNo_shelfId_productId_ListMap.keySet()) {
            StockTakeTask s = new StockTakeTask();
            s.setTenantId(req.getTenantId());
            s.setStockTakeId(req.getStockTakeId());
            s.setAssignType(req.getAssignType());
            s.setStockTakeNo(stockTake.getStockTakeNo());
            s.setTaskNo(stockTake.getStockTakeNo() + "-" + generateStockTakeNo(req.getTenantId()));
            s.setWarehouseId(stockTake.getWarehouseId());
            s.setStockTakeType(stockTake.getTakeType());
            s.setAssigneeId(req.getUserId());
            s.setExecutorId(userId);
            s.setTaskStatus(CkStockTakeEnums.StockTakeTaskStatus.ASSIGNED.getCode());
            s.setTotalItems(needAssignTask.size());
            s.setCompletedItems(0);
            s.setPlanStartTime(req.getPlanStartTime());
            s.setPlanEndTime(req.getPlanEndTime());
            s.setPriority(req.getPriority());
            s.setRemark(req.getRemark());
            s.setLocationRange(JSON.toJSONString(userId2ItemIdsMap.get(userId)));
            saveBatchList.add(s);
        }
        return saveBatchList;
    }

    public List<StockListPageResp> completedStockTakeList(Long warehouseId, Long tenantId) {
        List<StockTake> list = stockTakeService.selectByWarehouseId(warehouseId, tenantId);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        Map<Long, List<StockTakeItem>> stockTakeId2ItemListMap = new HashMap<>();
        List<Long> stockTakeIds = list.stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(stockTakeIds)) {
            List<StockTakeItem> itemList = stockTaskItemService.selectByStockTakeIds(stockTakeIds, tenantId);
            stockTakeId2ItemListMap = itemList.stream().collect(Collectors.groupingBy(StockTakeItem::getStockTakeId));
        }

        Map<Long, Account> userId2AccountMap = new HashMap<>();
        List<Long> userIds = list.stream().map(v -> v.getCreatedBy()).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(userIds)) {
            List<Account> accounts = accountService.selectByIds(userIds);
            userId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        }

        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Long> warehouseIds = list.stream().map(v -> v.getWarehouseId()).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(warehouseIds)) {
            List<Warehouse> warehouseList = warehouseService.selectByTenantIdAndWareHouseIds(tenantId, warehouseIds);
            warehouseId2WarehouseMap = warehouseList.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        Map<Long, List<StockTakeItem>> finalStockTakeId2ItemListMap = stockTakeId2ItemListMap;
        Map<Long, Account> finalUserId2AccountMap = userId2AccountMap;
        Map<Long, Warehouse> finalWarehouseId2WarehouseMap = warehouseId2WarehouseMap;
        return  list.stream().map(v -> {
            StockListPageResp p = new StockListPageResp();
            BeanUtils.copyProperties(v, p);


            List<StockTakeItem> items = finalStockTakeId2ItemListMap.getOrDefault(v.getId(), new ArrayList<>());
            p.setItemCount(CollectionUtils.isEmpty(items) ? 0 : items.size());

            List<StockTakeItem> collect1 = items.stream().filter(s -> !CkStockTakeEnums.StockItemStatus.NOT_ADJUSTED.getCode().equals(s.getStatus())).collect(Collectors.toList());
            p.setCountedCount(collect1.size());

            List<StockTakeItem> diffList = items.stream().filter(s -> s.getDiffQuantity() != null && s.getDiffQuantity().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
            p.setTotalDiff(diffList.size());

            p.setCreatedByName(finalUserId2AccountMap.getOrDefault(v.getCreatedBy(), new Account()).getUsername());
            p.setWarehouseName(finalWarehouseId2WarehouseMap.getOrDefault(v.getWarehouseId(), new Warehouse()).getName());
            return p;
        }).collect(Collectors.toList());

    }
}

