package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.AdjustOrder;
import com.example.entity.cangku.dto.AdjustOrderItem;
import com.example.entity.cangku.req.AdjustApproveOkReq;
import com.example.entity.cangku.req.AdjustListPageReq;
import com.example.entity.cangku.req.AdjustRequest;
import com.example.entity.cangku.req.AdjustSubmitApproveReq;
import com.example.entity.cangku.resp.AdjustOrderResp;
import com.example.enums.CkAdjustEnums;
import com.example.service.CkAdjustOrderItemService;
import com.example.service.CkAdjustOrderService;
import com.example.utils.DateUtils;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/29 15:23
 */
@Service
public class CkAdjustOrderFacade {
    @Resource
    CkAdjustOrderService adjustOrderService;
    @Resource
    CkAdjustOrderItemService adjustOrderItemService;


    @Transactional(rollbackFor = Exception.class)
    public Boolean create(AdjustRequest req) {
        // 1. 生成调整单号（如果前端未提供）
        if (req.getAdjustNo() == null || req.getAdjustNo().trim().isEmpty()) {
            String adjustNo = generateAdjustNo(req.getTenantId());
            req.setAdjustNo(adjustNo);
        }

        // 2. 计算统计信息
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalCostAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (AdjustRequest.AdjustItemRequest item : req.getItems()) {
            // 计算调整数量的绝对值
            BigDecimal absQuantity = item.getAdjustQuantity().abs();
            totalQuantity = totalQuantity.add(absQuantity);

            // 计算调整成本金额
            if (item.getUnitCost() != null) {
                BigDecimal costAmount = item.getAdjustQuantity().multiply(item.getUnitCost());
                totalCostAmount = totalCostAmount.add(costAmount);
            }

            // 计算调整金额
            if (item.getUnitPrice() != null) {
                BigDecimal amount = item.getAdjustQuantity().multiply(item.getUnitPrice());
                totalAmount = totalAmount.add(amount);
            }
        }

        // 3. 保存调整单主表
        AdjustOrder order = new AdjustOrder();
        order.setAdjustNo(req.getAdjustNo());
        order.setWarehouseId(req.getWarehouseId());
        order.setAdjustType(req.getAdjustType());
        order.setSourceType(req.getSourceType());
        order.setSourceId(req.getSourceId());
        order.setSourceNo(req.getSourceNo());
        order.setAdjustReason(req.getAdjustReason());
        order.setIsAffectCost(req.getIsAffectCost());
        order.setIsUrgent(req.getIsUrgent());
        order.setPriority(req.getPriority() != null ? req.getPriority() : 3); // 默认中优先级
        order.setRemark(req.getRemark());

        // 设置状态信息
        order.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.WaitSubmit.getCode()); // 待提交

        // 设置统计信息
        order.setTotalItems(req.getItems().size());
        order.setTotalQuantity(totalQuantity);
        order.setTotalCostAmount(totalCostAmount);
        order.setTotalAmount(totalAmount);

        // 设置时间信息
        order.setExpectExecuteTime(DateUtils.str2Date(req.getExpectExecuteTime()));

        //设置审核信息
        order.setApproverId(req.getApprovalUserId());

        // 设置基础信息
        order.setTenantId(req.getTenantId());
        order.setCreatedBy(req.getUserId());
        order.setCreatedAt(new Date());
        order.setModifiedBy(req.getUserId());
        order.setModifiedAt(new Date());
        order.setIsDeleted(0);
        order.setVersion(0);

        // 保存主表
        boolean save = adjustOrderService.save(order);
        if (!save) {
            throw new ValidationException("保存调整单失败");
        }

        // 4. 保存调整单明细
        List<AdjustOrderItem> items = req.getItems().stream().map(itemReq -> {
            AdjustOrderItem item = new AdjustOrderItem();

            // 基础信息
            item.setAdjustOrderId(order.getId());
            item.setAdjustNo(order.getAdjustNo());
            item.setTenantId(req.getTenantId());

            // 产品信息
            item.setProductId(itemReq.getProductId());
            item.setProductName(itemReq.getProductName());
            item.setProductCode(itemReq.getProductCode());
            item.setSkuCode(itemReq.getSkuCode());
            item.setSpecification(itemReq.getSpec());
            item.setUnit(itemReq.getUnit());

            // 库存信息
            item.setWarehouseId(req.getWarehouseId());
            item.setWarehouseName(req.getWarehouseName());
            item.setBatchNo(itemReq.getBatchNo());
            item.setShelfId(itemReq.getShelfId());
            item.setShelfCode(itemReq.getShelfCode());
            item.setLocationCode(itemReq.getLocationCode());

            // 数量信息
            item.setBeforeQuantity(itemReq.getBeforeQuantity());
            item.setAdjustQuantity(itemReq.getAdjustQuantity());
            item.setAfterQuantity(itemReq.getAfterQuantity());

            // 成本金额信息
            item.setUnitCost(itemReq.getUnitCost());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setAdjustCostAmount(itemReq.getAdjustCostAmount());
            item.setAdjustAmount(itemReq.getAdjustAmount());

            // 计算成本金额和调整金额（如果未提供）
            if (item.getAdjustCostAmount() == null && item.getUnitCost() != null) {
                BigDecimal costAmount = item.getAdjustQuantity().multiply(item.getUnitCost());
                item.setAdjustCostAmount(costAmount);
            }

            if (item.getAdjustAmount() == null && item.getUnitPrice() != null) {
                BigDecimal amount = item.getAdjustQuantity().multiply(item.getUnitPrice());
                item.setAdjustAmount(amount);
            }

            // 其他信息
            item.setAdjustReason(itemReq.getItemReason() != null ? itemReq.getItemReason() : req.getAdjustReason());
            item.setItemRemark(itemReq.getItemRemark());
            item.setInventoryType(itemReq.getInventoryType() != null ? itemReq.getInventoryType() : 1);
            item.setIsAffectCost(req.getIsAffectCost());
            item.setCostAdjustMethod(itemReq.getCostAdjustMethod() != null ? itemReq.getCostAdjustMethod() : 1);
            item.setSourceItemId(itemReq.getSourceItemId());
            item.setStatus(1); // 待执行
            item.setExtData(itemReq.getExtData());

            // 基础字段
            item.setCreatedBy(req.getUserId());
            item.setCreatedAt(new Date());
            item.setModifiedBy(req.getUserId());
            item.setModifiedAt(new Date());
            item.setIsDeleted(0);

            return item;
        }).collect(Collectors.toList());

        // 批量保存明细
        boolean saveBatch = adjustOrderItemService.saveBatch(items);
        if (!saveBatch) {
            throw new ValidationException("保存调整单明细失败");
        }

        return true;
    }

    /**
     * 生成调整单号
     */
    private String generateAdjustNo(Long tenantId) {
        // 示例：TZ202512250001
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        // 这里需要查询当天的最大流水号，这里简化为示例
        String sequence = String.format("%04d", 1); // 实际应该从数据库获取
        return "TZ" + dateStr + sequence;
    }

    public Page<AdjustOrderResp> pageList(Page<AdjustOrder> page, AdjustListPageReq req) {
        Page<AdjustOrder> list = adjustOrderService.getStockPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

//        Map<Long, List<StockTakeItem>> stockTakeId2ItemListMap = new HashMap<>();
//        List<Long> stockTakeIds = list.getRecords().stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
//        if (!CollectionUtils.isEmpty(stockTakeIds)) {
//            List<StockTakeItem> itemList = stockTaskItemService.selectByStockTakeIds(stockTakeIds, req.getTenantId());
//            stockTakeId2ItemListMap = itemList.stream().collect(Collectors.groupingBy(StockTakeItem::getStockTakeId));
//        }
//
//        Map<Long, Account> userId2AccountMap = new HashMap<>();
//        List<Long> userIds = list.getRecords().stream().map(v -> v.getCreatedBy()).distinct().collect(Collectors.toList());
//        if (!CollectionUtils.isEmpty(userIds)) {
//            List<Account> accounts = accountService.selectByIds(userIds);
//            userId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
//        }
//
//        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
//        List<Long> warehouseIds = list.getRecords().stream().map(v -> v.getWarehouseId()).distinct().collect(Collectors.toList());
//        if (!CollectionUtils.isEmpty(warehouseIds)) {
//            List<Warehouse> warehouseList = warehouseService.selectByTenantIdAndWareHouseIds(req.getTenantId(), warehouseIds);
//            warehouseId2WarehouseMap = warehouseList.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
//        }
//
//        Map<Long, List<StockTakeItem>> finalStockTakeId2ItemListMap = stockTakeId2ItemListMap;
//        Map<Long, Account> finalUserId2AccountMap = userId2AccountMap;
//        Map<Long, Warehouse> finalWarehouseId2WarehouseMap = warehouseId2WarehouseMap;
        List<AdjustOrderResp> collect = list.getRecords().stream().map(v -> {
            AdjustOrderResp p = new AdjustOrderResp();
            BeanUtils.copyProperties(v, p);


//            List<StockTakeItem> items = finalStockTakeId2ItemListMap.getOrDefault(v.getId(), new ArrayList<>());
//            p.setItemCount(CollectionUtils.isEmpty(items) ? 0 : items.size());
//
//            List<StockTakeItem> collect1 = items.stream().filter(s -> !CkStockTakeEnums.StockItemStatus.NOT_ADJUSTED.getCode().equals(s.getStatus())).collect(Collectors.toList());
//            p.setCountedCount(collect1.size());
//
//            List<StockTakeItem> diffList = items.stream().filter(s -> s.getDiffQuantity() != null && s.getDiffQuantity().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
//            p.setTotalDiff(diffList.size());
//
//            p.setCreatedByName(finalUserId2AccountMap.getOrDefault(v.getCreatedBy(), new Account()).getUsername());
//            p.setWarehouseName(finalWarehouseId2WarehouseMap.getOrDefault(v.getWarehouseId(), new Warehouse()).getName());
            return p;
        }).collect(Collectors.toList());

        Page<AdjustOrderResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean submitApprove(AdjustSubmitApproveReq req) {
        AdjustOrder adjustOrder = adjustOrderService.selectById(req.getId(), req.getTenantId());
        if (adjustOrder == null) {
            throw new ValidationException("调整单不存在");
        }
        if (!CkAdjustEnums.AdjustOrderStatus.WaitSubmit.getCode().equals(adjustOrder.getAdjustStatus())) {
            throw new ValidationException("调整单状态错误");
        }
        adjustOrder.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.WaitAudit.getCode());
        adjustOrder.setModifiedBy(req.getUserId());
        adjustOrder.setModifiedAt(new Date());
        boolean updated = adjustOrderService.updateStatusById(adjustOrder.getId(), adjustOrder.getTenantId(), CkAdjustEnums.AdjustOrderStatus.WaitAudit.getCode());
        if (!updated) {
            throw new ValidationException("请求审核调整单失败");
        }
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean approveOk(AdjustApproveOkReq req) {
        AdjustOrder adjustOrder = adjustOrderService.selectById(req.getId(), req.getTenantId());
        if (adjustOrder == null) {
            throw new ValidationException("调整单不存在");
        }
        if (!CkAdjustEnums.AdjustOrderStatus.WaitAudit.getCode().equals(adjustOrder.getAdjustStatus())) {
            throw new ValidationException("调整单状态错误");
        }
        if (!(CkAdjustEnums.AdjustOrderStatus.AuditPass.getCode().equals(req.getApproveStatus())|| CkAdjustEnums.AdjustOrderStatus.Reject.getCode().equals(req.getApproveStatus()))){
            throw new ValidationException("提交状态错误");
        }
        adjustOrder.setApproveTime(new Date());
        adjustOrder.setApproveRemark(req.getApproveRemark());
        adjustOrder.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.AuditPass.getCode());
        adjustOrder.setModifiedBy(req.getUserId());
        adjustOrder.setModifiedAt(new Date());
        boolean updated = adjustOrderService.updateStatusAndRemarkById(adjustOrder.getId(), adjustOrder.getTenantId(), CkAdjustEnums.AdjustOrderStatus.AuditPass.getCode(), req.getApproveRemark());
        if (!updated) {
            throw new ValidationException("审核调整单失败");
        }
        return true;
    }


}
