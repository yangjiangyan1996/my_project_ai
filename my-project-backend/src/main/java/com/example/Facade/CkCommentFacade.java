package com.example.Facade;

import com.example.entity.cangku.req.InboundProductUsedShelfReq;
import com.example.entity.cangku.req.ShelfProductUsedAllReq;
import com.example.entity.cangku.resp.InboundProductUsedShelfResp;
import com.example.entity.cangku.resp.ProductUsedShelfResp;
import com.example.entity.cangku.resp.ShelfPageListResp;
import com.example.entity.cangku.resp.ShelfProductUsedAllResp;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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

    /**
     * 按照批次创建时间顺序分配货架容量
     * @param reqList 请求参数列表
     * @return 分配结果列表
     */
    public List<ShelfProductUsedAllResp> allocateShelfInventoryQuantity(List<ShelfProductUsedAllReq> reqList) {
        // 1. 数据验证
        validateRequest(reqList);

        // 2. 构建批次容量映射（按商品分组）
        Map<Long, Map<String, Map<Long, BigDecimal>>> productBatchShelfCapacityMap =
                buildProductBatchShelfCapacityMap(reqList);

        // 3. 获取所有货架信息
        List<Long> allShelfIds = collectAllShelfIds(productBatchShelfCapacityMap);
        Map<Long, String> shelfIdToNameMap = getShelfNames(allShelfIds);

        // 4. 获取常用货架（按商品）
        List<Long> productIds = reqList.stream()
                .map(ShelfProductUsedAllReq::getProductId)
                .collect(Collectors.toList());
        Map<Long, List<Long>> productCommonShelvesMap = getProductCommonShelves(productIds);

        // 5. 按商品逐个分配
        List<ShelfProductUsedAllResp> result = new ArrayList<>();

        for (ShelfProductUsedAllReq productReq : reqList) {
            ShelfProductUsedAllResp allocationResult = allocateProductByBatchOrder(
                    productReq,
                    productBatchShelfCapacityMap,
                    productCommonShelvesMap.getOrDefault(productReq.getProductId(), Collections.emptyList()),
                    shelfIdToNameMap
            );
            result.add(allocationResult);
        }

        return result;
    }

    /**
     * 构建商品-批次-货架容量映射
     * 结构: Map<productId, Map<batchNo, Map<shelfId, capacity>>>
     */
    private Map<Long, Map<String, Map<Long, BigDecimal>>> buildProductBatchShelfCapacityMap(
            List<ShelfProductUsedAllReq> reqList) {

        Map<Long, Map<String, Map<Long, BigDecimal>>> result = new HashMap<>();

        for (ShelfProductUsedAllReq productReq : reqList) {
            Long productId = productReq.getProductId();

            if (productId == null || productReq.getPcList() == null) {
                continue;
            }

            // 初始化商品映射
            Map<String, Map<Long, BigDecimal>> batchMap =
                    result.computeIfAbsent(productId, k -> new HashMap<>());

            for (ShelfProductUsedAllReq.ShelfProductUsedAllOfPcReq batchReq : productReq.getPcList()) {
                String batchNo = batchReq.getBatNo();

                if (batchNo == null || batchReq.getShelfQuantityList() == null) {
                    continue;
                }

                // 初始化批次映射
                Map<Long, BigDecimal> shelfMap =
                        batchMap.computeIfAbsent(batchNo, k -> new HashMap<>());

                // 累加货架容量（相同货架的数量相加）
                for (ShelfProductUsedAllReq.ShelfProductUsedAllOfShelfQuantityReq shelfReq :
                        batchReq.getShelfQuantityList()) {

                    Long shelfId = shelfReq.getShelfId();
                    BigDecimal quantity = shelfReq.getQuantity();

                    if (shelfId != null && quantity != null) {
                        shelfMap.merge(shelfId, quantity, BigDecimal::add);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 按批次顺序为单个商品分配货架容量
     */
    private ShelfProductUsedAllResp allocateProductByBatchOrder(
            ShelfProductUsedAllReq productReq,
            Map<Long, Map<String, Map<Long, BigDecimal>>> productBatchShelfCapacityMap,
            List<Long> commonShelves,
            Map<Long, String> shelfIdToNameMap) {

        ShelfProductUsedAllResp resp = new ShelfProductUsedAllResp();
        resp.setIndex(productReq.getIndex());
        resp.setProductId(productReq.getProductId());
        resp.setSku(productReq.getSku());

        // 需要分配的商品数量
        BigDecimal remainingQuantity = productReq.getQuantity();
        List<ShelfProductUsedAllResp.BatchAllocation> batchAllocations = new ArrayList<>();

        // 1. 获取该商品的批次-货架容量映射
        Map<String, Map<Long, BigDecimal>> batchShelfCapacityMap =
                productBatchShelfCapacityMap.getOrDefault(productReq.getProductId(), new HashMap<>());

        // 2. 获取批次列表并按创建时间排序（早的优先）
        List<BatchInfo> sortedBatches = getSortedBatches(productReq.getPcList());

        // 3. 按批次顺序分配
        for (BatchInfo batchInfo : sortedBatches) {
            if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            String batchNo = batchInfo.getBatchNo();

            // 获取该批次的货架容量
            Map<Long, BigDecimal> shelfCapacityMap =
                    batchShelfCapacityMap.getOrDefault(batchNo, new HashMap<>());

            // 创建批次分配结果
            ShelfProductUsedAllResp.BatchAllocation batchAlloc =
                    allocateBatch(batchNo, batchInfo.getCreatedAt(), remainingQuantity,
                            shelfCapacityMap, commonShelves, shelfIdToNameMap);

            if (batchAlloc != null) {
                // 计算本次分配的总量
                BigDecimal batchAllocated = batchAlloc.getAllocations().stream()
                        .map(ShelfProductUsedAllResp.ShelfAllocation::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 更新剩余数量
                remainingQuantity = remainingQuantity.subtract(batchAllocated);

                // 更新全局容量
                updateBatchCapacity(batchShelfCapacityMap, batchNo, batchAlloc.getAllocations());

                batchAllocations.add(batchAlloc);
            }
        }

        // 4. 检查是否完全分配
        if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            // 如果还有剩余，说明货架容量不足
            throw new ValidationException(String.format(
                    "商品 %s(ID:%d) 分配不完整，剩余 %.4f 无货架容量",
                    productReq.getSku(), productReq.getProductId(), remainingQuantity));
        }

        // 5. 更新总映射
        if (!batchShelfCapacityMap.isEmpty()) {
            productBatchShelfCapacityMap.put(productReq.getProductId(), batchShelfCapacityMap);
        }

        resp.setBatchAllocations(batchAllocations);
        return resp;
    }

    /**
     * 为单个批次分配货架容量
     */
    private ShelfProductUsedAllResp.BatchAllocation allocateBatch(
            String batchNo,
            Date batchCreatedAt,
            BigDecimal remainingQuantity,
            Map<Long, BigDecimal> shelfCapacityMap,
            List<Long> commonShelves,
            Map<Long, String> shelfIdToNameMap) {

        ShelfProductUsedAllResp.BatchAllocation batchAlloc = new ShelfProductUsedAllResp.BatchAllocation();
        batchAlloc.setBatchNo(batchNo);
        batchAlloc.setCreatedAtOfBatch(batchCreatedAt);

        List<ShelfProductUsedAllResp.ShelfAllocation> shelfAllocations = new ArrayList<>();
        BigDecimal remaining = remainingQuantity;

        // 1. 优先分配指定货架（按容量从大到小）
        remaining = allocateSpecifiedShelves(remaining, shelfCapacityMap,
                shelfAllocations, shelfIdToNameMap);

        // 2. 如果还有剩余，分配常用货架
        if (remaining.compareTo(BigDecimal.ZERO) > 0 && commonShelves != null) {
            remaining = allocateCommonShelves(remaining, shelfCapacityMap, commonShelves,
                    shelfAllocations, shelfIdToNameMap);
        }

        // 3. 如果还有剩余，分配其他可用货架
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            remaining = allocateOtherShelves(remaining, shelfCapacityMap,
                    shelfAllocations, shelfIdToNameMap);
        }

        // 如果分配了货架，则返回结果
        if (!shelfAllocations.isEmpty()) {
            batchAlloc.setAllocations(shelfAllocations);
            return batchAlloc;
        }

        return null;
    }

    /**
     * 分配指定货架
     */
    private BigDecimal allocateSpecifiedShelves(
            BigDecimal remainingQuantity,
            Map<Long, BigDecimal> shelfCapacityMap,
            List<ShelfProductUsedAllResp.ShelfAllocation> allocations,
            Map<Long, String> shelfIdToNameMap) {

        BigDecimal remaining = remainingQuantity;

        // 按容量从大到小排序
        List<Map.Entry<Long, BigDecimal>> sortedShelves = shelfCapacityMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());

        for (Map.Entry<Long, BigDecimal> entry : sortedShelves) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            Long shelfId = entry.getKey();
            BigDecimal capacity = entry.getValue();

            if (capacity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal allocateQty = capacity.min(remaining);

            allocations.add(new ShelfProductUsedAllResp.ShelfAllocation(
                    shelfId,
                    shelfIdToNameMap.getOrDefault(shelfId, "未知货架"),
                    allocateQty
            ));

            // 更新容量
            shelfCapacityMap.put(shelfId, capacity.subtract(allocateQty));
            remaining = remaining.subtract(allocateQty);
        }

        return remaining;
    }

    /**
     * 分配常用货架
     */
    private BigDecimal allocateCommonShelves(
            BigDecimal remainingQuantity,
            Map<Long, BigDecimal> shelfCapacityMap,
            List<Long> commonShelves,
            List<ShelfProductUsedAllResp.ShelfAllocation> allocations,
            Map<Long, String> shelfIdToNameMap) {

        BigDecimal remaining = remainingQuantity;

        // 已经分配过的货架ID
        Set<Long> allocatedShelfIds = allocations.stream()
                .map(ShelfProductUsedAllResp.ShelfAllocation::getShelfId)
                .collect(Collectors.toSet());

        for (Long shelfId : commonShelves) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // 跳过已分配的货架
            if (allocatedShelfIds.contains(shelfId)) {
                continue;
            }

            BigDecimal capacity = shelfCapacityMap.getOrDefault(shelfId, BigDecimal.ZERO);
            if (capacity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal allocateQty = capacity.min(remaining);

            allocations.add(new ShelfProductUsedAllResp.ShelfAllocation(
                    shelfId,
                    shelfIdToNameMap.getOrDefault(shelfId, "未知货架"),
                    allocateQty
            ));

            // 更新容量
            shelfCapacityMap.put(shelfId, capacity.subtract(allocateQty));
            remaining = remaining.subtract(allocateQty);
            allocatedShelfIds.add(shelfId);
        }

        return remaining;
    }

    /**
     * 分配其他货架
     */
    private BigDecimal allocateOtherShelves(
            BigDecimal remainingQuantity,
            Map<Long, BigDecimal> shelfCapacityMap,
            List<ShelfProductUsedAllResp.ShelfAllocation> allocations,
            Map<Long, String> shelfIdToNameMap) {

        BigDecimal remaining = remainingQuantity;

        // 已经分配过的货架ID
        Set<Long> allocatedShelfIds = allocations.stream()
                .map(ShelfProductUsedAllResp.ShelfAllocation::getShelfId)
                .collect(Collectors.toSet());

        // 按容量从大到小排序（排除已分配的）
        List<Map.Entry<Long, BigDecimal>> availableShelves = shelfCapacityMap.entrySet().stream()
                .filter(entry -> !allocatedShelfIds.contains(entry.getKey()))
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());

        for (Map.Entry<Long, BigDecimal> entry : availableShelves) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            Long shelfId = entry.getKey();
            BigDecimal capacity = entry.getValue();

            BigDecimal allocateQty = capacity.min(remaining);

            allocations.add(new ShelfProductUsedAllResp.ShelfAllocation(
                    shelfId,
                    shelfIdToNameMap.getOrDefault(shelfId, "未知货架"),
                    allocateQty
            ));

            // 更新容量
            shelfCapacityMap.put(shelfId, capacity.subtract(allocateQty));
            remaining = remaining.subtract(allocateQty);
        }

        return remaining;
    }

    /**
     * 更新批次容量
     */
    private void updateBatchCapacity(
            Map<String, Map<Long, BigDecimal>> batchShelfCapacityMap,
            String batchNo,
            List<ShelfProductUsedAllResp.ShelfAllocation> allocations) {

        Map<Long, BigDecimal> shelfMap = batchShelfCapacityMap.getOrDefault(batchNo, new HashMap<>());

        for (ShelfProductUsedAllResp.ShelfAllocation allocation : allocations) {
            Long shelfId = allocation.getShelfId();
            BigDecimal allocatedQty = allocation.getQuantity();

            if (shelfMap.containsKey(shelfId)) {
                BigDecimal remaining = shelfMap.get(shelfId).subtract(allocatedQty);
                if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                    shelfMap.put(shelfId, remaining);
                } else {
                    shelfMap.remove(shelfId);
                }
            }
        }

        // 如果该批次所有货架容量都已用完，移除该批次
        if (shelfMap.isEmpty()) {
            batchShelfCapacityMap.remove(batchNo);
        } else {
            batchShelfCapacityMap.put(batchNo, shelfMap);
        }
    }

    /**
     * 获取排序后的批次列表
     */
    private List<BatchInfo> getSortedBatches(List<ShelfProductUsedAllReq.ShelfProductUsedAllOfPcReq> pcList) {
        if (pcList == null) {
            return Collections.emptyList();
        }

        // 提取批次信息
        List<BatchInfo> batches = pcList.stream()
                .filter(pc -> pc.getBatNo() != null)
                .map(pc -> new BatchInfo(pc.getBatNo(), pc.getCreatedAtOfBatch()))
                .collect(Collectors.toList());

        // 去重（按批次号去重，保留创建时间最早的）
        Map<String, BatchInfo> uniqueBatches = batches.stream()
                .collect(Collectors.toMap(
                        BatchInfo::getBatchNo,
                        Function.identity(),
                        (b1, b2) -> {
                            // 如果都有创建时间，取较早的
                            if (b1.getCreatedAt() != null && b2.getCreatedAt() != null) {
                                return b1.getCreatedAt().before(b2.getCreatedAt()) ? b1 : b2;
                            }
                            // 如果只有一个有创建时间，取有的
                            return b1.getCreatedAt() != null ? b1 : b2;
                        }
                ));

        // 按创建时间排序（早的优先）
        return uniqueBatches.values().stream()
                .sorted((b1, b2) -> {
                    if (b1.getCreatedAt() == null && b2.getCreatedAt() == null) return 0;
                    if (b1.getCreatedAt() == null) return 1;
                    if (b2.getCreatedAt() == null) return -1;
                    return b1.getCreatedAt().compareTo(b2.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    /**
     * 收集所有货架ID
     */
    private List<Long> collectAllShelfIds(
            Map<Long, Map<String, Map<Long, BigDecimal>>> productBatchShelfCapacityMap) {

        return productBatchShelfCapacityMap.values().stream()
                .flatMap(batchMap -> batchMap.values().stream())
                .flatMap(shelfMap -> shelfMap.keySet().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取货架名称映射
     */
    private Map<Long, String> getShelfNames(List<Long> shelfIds) {
        // 这里需要根据实际情况实现，从数据库或服务获取货架名称
        // 示例：返回空映射，实际应该调用相关服务
        return Collections.emptyMap();
    }

    /**
     * 获取商品常用货架
     */
    private Map<Long, List<Long>> getProductCommonShelves(List<Long> productIds) {
        // 这里需要根据实际情况实现，获取商品的常用货架
        // 示例：返回空映射，实际应该调用相关服务
        return Collections.emptyMap();
    }

    /**
     * 数据验证
     */
    private void validateRequest(List<ShelfProductUsedAllReq> reqList) {
        if (reqList == null || reqList.isEmpty()) {
            throw new ValidationException("请求参数不能为空");
        }

        for (ShelfProductUsedAllReq req : reqList) {
            if (req.getProductId() == null) {
                throw new ValidationException("商品ID不能为空");
            }

            if (req.getQuantity() == null || req.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException(String.format(
                        "商品 %s(ID:%d) 数量必须大于0", req.getSku(), req.getProductId()));
            }

            if (req.getPcList() == null || req.getPcList().isEmpty()) {
                throw new ValidationException(String.format(
                        "商品 %s(ID:%d) 批次列表不能为空", req.getSku(), req.getProductId()));
            }
        }
    }

    /**
     * 批次信息辅助类
     */
    @Data
    @AllArgsConstructor
    private static class BatchInfo {
        private String batchNo;
        private Date createdAt;
    }
}
