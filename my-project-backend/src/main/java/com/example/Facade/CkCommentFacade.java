package com.example.Facade;

import com.example.entity.cangku.req.InboundProductUsedShelfReq;
import com.example.entity.cangku.req.ShelfProductUsedAllReq;
import com.example.entity.cangku.resp.InboundProductUsedShelfResp;
import com.example.entity.cangku.resp.ProductUsedShelfResp;
import com.example.entity.cangku.resp.ShelfPageListResp;
import com.example.entity.cangku.resp.ShelfProductUsedAllResp;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/12 00:05
 */
@Service
public class CkCommentFacade {
    @Resource
    private CkInventoryFacade inventoryFacade;
    @Resource
    private CkShelfFacade shelfFacade;

    public List<InboundProductUsedShelfResp> allocateIShelfnventoryQuantity(InboundProductUsedShelfReq req) {
        List<Long> productIds = req.getList().stream().map(v -> v.getProductId()).collect(Collectors.toList());
        List<ProductUsedShelfResp> commonlyUsedShelvesForGoods = inventoryFacade.getCommonlyUsedShelvesForGoods(productIds, req.getTenantId());
        List<ShelfPageListResp> shelfList = shelfFacade.listEnable(req.getTenantId(), req.getWarehouseId());

        //商品ID对应的需要分配的数量
        Map<Long, BigDecimal> productId2QuantityMap = req.getList().stream().collect(Collectors.toMap(v -> v.getProductId(), v -> v.getQuantity()));

        Map<Long, String> productId2SkuMap = req.getList().stream().collect(Collectors.toMap(v -> v.getProductId(), v -> v.getSku()));

        //货架ID对应货架剩余可用数量
        Map<Long, BigDecimal> shelfId2AvailableCapacityMap = shelfList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v.getAvailableCapacity()));

        //商品ID对应商品常用的货架ID
        Map<Long, List<Long>> productId2UsedShelfIdsMap = commonlyUsedShelvesForGoods.stream().collect(Collectors.toMap(v -> v.getProductId(), v -> v.getShelfIds()));

        List<InboundProductUsedShelfResp> result = new ArrayList<>();

        //开始分配
        for (Map.Entry<Long, BigDecimal> entry : productId2QuantityMap.entrySet()) {

            Long productId = entry.getKey();
            BigDecimal remainQty = entry.getValue(); // 还需要分配的数量

            InboundProductUsedShelfResp resp = new InboundProductUsedShelfResp();
            resp.setProductId(productId);
            List<InboundProductUsedShelfResp.ProductUsedShelfRespInner> allocList = new ArrayList<>();

            // 1. 优先分配常用货架
            List<Long> preferredShelves = productId2UsedShelfIdsMap.getOrDefault(productId, Collections.emptyList());

            for (Long shelfId : preferredShelves) {
                if (remainQty.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal available = shelfId2AvailableCapacityMap.getOrDefault(shelfId, BigDecimal.ZERO);

                if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal assignQty = available.min(remainQty);
                remainQty = remainQty.subtract(assignQty);
                shelfId2AvailableCapacityMap.put(shelfId, available.subtract(assignQty));

                allocList.add(new InboundProductUsedShelfResp.ProductUsedShelfRespInner(shelfId, assignQty));
            }


            // 2. 分配其他货架（排除常用货架）
            if (remainQty.compareTo(BigDecimal.ZERO) > 0) {

                for (ShelfPageListResp shelf : shelfList) {
                    if (remainQty.compareTo(BigDecimal.ZERO) <= 0) break;

                    Long shelfId = shelf.getId();
                    if (preferredShelves.contains(shelfId)) continue;

                    BigDecimal available = shelfId2AvailableCapacityMap.getOrDefault(shelfId, BigDecimal.ZERO);
                    if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal assignQty = available.min(remainQty);
                    remainQty = remainQty.subtract(assignQty);
                    shelfId2AvailableCapacityMap.put(shelfId, available.subtract(assignQty));

                    allocList.add(new InboundProductUsedShelfResp.ProductUsedShelfRespInner(shelfId, assignQty));
                }
            }


            // 3. 若还有剩余，说明货架容量不足 —— 给你暴露接口，可用于前端提示
            if (remainQty.compareTo(BigDecimal.ZERO) > 0) {
                // 你可以用抛异常，也可以在 resp 里加字段记录
                String sku = productId2SkuMap.get(productId);
                throw new ValidationException("商品sku " + sku + " 货架容量不足，剩余未分配数量：" + remainQty);
            }

            resp.setShelfQuantityList(allocList);
            result.add(resp);
        }

        return result;
    }

    public List<ShelfProductUsedAllResp> allocateIShelfnventoryQuantity(ShelfProductUsedAllReq req) {
        List<Long> productIds = req.getList().stream().map(v -> v.getProductId()).collect(Collectors.toList());
        List<ProductUsedShelfResp> commonlyUsedShelvesForGoods = inventoryFacade.getCommonlyUsedShelvesForGoods(productIds, req.getTenantId());
        List<ShelfPageListResp> shelfList = shelfFacade.listEnable(req.getTenantId(), req.getWarehouseId());
        // 1. 基础数据准备
        Map<Long, String> productId2SkuMap = req.getList().stream()
                .collect(Collectors.toMap(ShelfProductUsedAllReq.ShelfProductUsedAllReqInner::getProductId,
                        ShelfProductUsedAllReq.ShelfProductUsedAllReqInner::getSku));

        // 2. 检查货架容量
        Map<Long, BigDecimal> shelfId2AvailableCapacityMap = shelfList.stream()
                .collect(Collectors.toMap(ShelfPageListResp::getId,
                        ShelfPageListResp::getAvailableCapacity));

        // 3. 常用货架映射
        Map<Long, List<Long>> productId2UsedShelfIdsMap = commonlyUsedShelvesForGoods.stream()
                .collect(Collectors.toMap(ProductUsedShelfResp::getProductId,
                        ProductUsedShelfResp::getShelfIds));

        // 4. 结果集
        List<ShelfProductUsedAllResp> result = new ArrayList<>();

        // 5. 遍历每个商品
        for (ShelfProductUsedAllReq.ShelfProductUsedAllReqInner productReq : req.getList()) {
            Long productId = productReq.getProductId();
            BigDecimal totalQtyNeeded = productReq.getQuantity();
            BigDecimal remainingQty = totalQtyNeeded;

            // 创建商品级别的响应
            ShelfProductUsedAllResp productResp = new ShelfProductUsedAllResp();
            productResp.setProductId(productId);
            productResp.setSku(productReq.getSku());

            List<ShelfProductUsedAllResp.BatchAllocation> batchAllocations = new ArrayList<>();

            // 6. 遍历商品的每个批次
            for (ShelfProductUsedAllReq.ShelfProductUsedAllOfPcReq batchReq : productReq.getPcList()) {
                Long batchNo = batchReq.getBatNo();
                BigDecimal batchRemaining = remainingQty.min(totalQtyNeeded); // 该批次还需要分配的数量

                // 创建批次级别的分配
                ShelfProductUsedAllResp.BatchAllocation batchAlloc = new ShelfProductUsedAllResp.BatchAllocation();
                batchAlloc.setBatchNo(batchNo);
                batchAlloc.setCreatedAtOfBatch(batchReq.getCreatedAtOfBatch());

                List<ShelfProductUsedAllResp.ShelfAllocation> shelfAllocations = new ArrayList<>();

                // 7. 优先分配批次中指定的货架
                for (ShelfProductUsedAllReq.ShelfProductUsedAllOfShelfQuantityReq shelfReq : batchReq.getShelfQuantityList()) {
                    if (batchRemaining.compareTo(BigDecimal.ZERO) <= 0) break;

                    Long shelfId = shelfReq.getShelfId();
                    BigDecimal requestedQty = shelfReq.getQuantity();
                    BigDecimal available = shelfId2AvailableCapacityMap.getOrDefault(shelfId, BigDecimal.ZERO);

                    if (available.compareTo(BigDecimal.ZERO) <= 0) {
                        continue; // 货架已满
                    }

                    // 计算实际分配量：取请求量、可用容量、剩余需求量的最小值
                    BigDecimal allocQty = requestedQty.min(available).min(batchRemaining);
                    if (allocQty.compareTo(BigDecimal.ZERO) <= 0) continue;

                    // 更新数据
                    batchRemaining = batchRemaining.subtract(allocQty);
                    available = available.subtract(allocQty);
                    remainingQty = remainingQty.subtract(allocQty);
                    shelfId2AvailableCapacityMap.put(shelfId, available);

                    // 记录分配结果
                    shelfAllocations.add(new ShelfProductUsedAllResp.ShelfAllocation(
                            shelfId,
                            shelfReq.getShelfName(),
                            allocQty
                    ));
                }

                // 8. 如果批次指定货架分配后仍有剩余，使用常用货架补充
                if (batchRemaining.compareTo(BigDecimal.ZERO) > 0) {
                    List<Long> preferredShelves = productId2UsedShelfIdsMap.getOrDefault(productId,
                            Collections.emptyList());

                    for (Long shelfId : preferredShelves) {
                        if (batchRemaining.compareTo(BigDecimal.ZERO) <= 0) break;

                        // 检查是否已在批次指定中分配过
                        boolean alreadyAllocated = batchReq.getShelfQuantityList().stream()
                                .anyMatch(s -> s.getShelfId().equals(shelfId));
                        if (alreadyAllocated) continue;

                        BigDecimal available = shelfId2AvailableCapacityMap.getOrDefault(shelfId, BigDecimal.ZERO);
                        if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

                        BigDecimal allocQty = available.min(batchRemaining);
                        batchRemaining = batchRemaining.subtract(allocQty);
                        available = available.subtract(allocQty);
                        remainingQty = remainingQty.subtract(allocQty);
                        shelfId2AvailableCapacityMap.put(shelfId, available);

                        // 获取货架名称
                        String shelfName = shelfList.stream()
                                .filter(s -> s.getId().equals(shelfId))
                                .map(ShelfPageListResp::getShelfName)
                                .findFirst()
                                .orElse("未知货架");

                        shelfAllocations.add(new ShelfProductUsedAllResp.ShelfAllocation(
                                shelfId, shelfName, allocQty
                        ));
                    }
                }

                // 9. 最后使用其他货架（排除已分配的）
                if (batchRemaining.compareTo(BigDecimal.ZERO) > 0) {
                    Set<Long> allocatedShelfIds = shelfAllocations.stream()
                            .map(ShelfProductUsedAllResp.ShelfAllocation::getShelfId)
                            .collect(Collectors.toSet());

                    for (ShelfPageListResp shelf : shelfList) {
                        if (batchRemaining.compareTo(BigDecimal.ZERO) <= 0) break;

                        Long shelfId = shelf.getId();
                        if (allocatedShelfIds.contains(shelfId)) continue;

                        BigDecimal available = shelfId2AvailableCapacityMap.getOrDefault(shelfId, BigDecimal.ZERO);
                        if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

                        BigDecimal allocQty = available.min(batchRemaining);
                        batchRemaining = batchRemaining.subtract(allocQty);
                        available = available.subtract(allocQty);
                        remainingQty = remainingQty.subtract(allocQty);
                        shelfId2AvailableCapacityMap.put(shelfId, available);

                        shelfAllocations.add(new ShelfProductUsedAllResp.ShelfAllocation(
                                shelfId, shelf.getShelfName(), allocQty
                        ));
                    }
                }

                // 设置批次分配结果
                batchAlloc.setAllocations(shelfAllocations);
                batchAlloc.setTotalAllocated(shelfAllocations.stream()
                        .map(ShelfProductUsedAllResp.ShelfAllocation::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

                batchAllocations.add(batchAlloc);
            }

            // 10. 检查商品是否完全分配
            if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
                String sku = productId2SkuMap.get(productId);
                throw new ValidationException(
                        String.format("商品 %s (ID: %d) 分配失败，剩余 %.2f 数量无法分配，货架容量不足",
                                sku, productId, remainingQty)
                );
            }

            productResp.setBatchAllocations(batchAllocations);
            productResp.setTotalAllocated(totalQtyNeeded);
            result.add(productResp);
        }

        return result;
    }
}
