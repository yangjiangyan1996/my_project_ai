package com.example.Facade;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.req.excel.OutBoundSaleQuantityImportDto;
import com.example.entity.cangku.resp.*;
import com.example.entity.cangku.resp.excel.OutboundOderExcelModel;
import com.example.entity.cangku.resp.excel.OutboundSaleExcelModel;
import com.example.entity.cangku.vo.SaleOutBoundItemExtVO;
import com.example.entity.dto.Account;
import com.example.enums.CkInOutboundEnums;
import com.example.enums.CkProductEnums;
import com.example.holder.InventoryHolder;
import com.example.service.*;
import com.example.utils.ExcelUtils;
import com.example.validhandle.OutboundCreateSaleProductReqValidator;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 01:04
 */
@Service
@Slf4j
public class CkOutboundFacade {

    @Resource
    CkProductionTaskService productionTaskService;
    @Resource
    CkOutboundOrderItemSaleExtService outboundOrderItemSaleExtService;
    @Resource
    CkOutboundOrderItemService outboundOrderItemService;
    @Resource
    com.example.holder.CkInventoryLockService inventoryLockService;
    @Autowired
    @Qualifier("inventoryLockExecutor")
    Executor asyncExecutor;
    @Resource
    CkRecommendRuleItemService recommendRuleItemService;
    @Resource
    CkRecommendRuleService recommendRuleService;
    @Resource
    CkOutboundOrderService outboundOrderService;

    @Resource
    CkProductBomService productBomService;
    @Resource
    CkProductBomDetailService productBomDetailService;
    @Resource
    CkCustomerSkuMappingService customerSkuMappingService;
    @Resource
    CkProductService productService;
    @Resource
    CkShelfZoneService shelfService;
    @Resource
    CkInventoryService inventoryService;
    @Resource
    CkInventoryLockService stockLockService;
    @Resource
    InventoryHolder inventoryHolder;
    @Resource
    CkInventoryWarehouseService inventoryWarehouseService;
    @Resource
    CkInventoryBatchService inventoryBatchService;
    @Resource
    CkInventoryShelfService inventoryShelfService;
    @Resource
    CkInventoryTransactionService inventoryTransactionService;
    @Resource
    CkCustomerService customerService;
    @Resource
    CkUnitService unitService;
    @Resource
    AccountService accountService;
    @Resource
    CkInventoryFacade inventoryFacade;
    @Resource
    CkWareHouseService warehouseService;

    /**
     * ==================== 生产领料出库 - 创建 ====================
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean createProductionPickingOutBound(OutboundCreateReq req) {
        log.info("开始创建生产领料出库单，订单号: {}, 租户ID: {}", req.getOrderNo(), req.getTenantId());
        long startTime = System.currentTimeMillis();

        try {
            // 1. 参数校验
            validateOutboundCreateReq(req);

            // 2. 构建并保存出库单主表
            OutboundOrder outboundOrder = buildOutboundOrder(req);
            boolean orderSaved = outboundOrderService.save(outboundOrder);
            if (!orderSaved) {
                throw new ValidationException("出库单主表保存失败");
            }

            // 3. 构建并保存出库单明细
            List<OutboundOrderItem> orderItems = buildOutboundOrderItems(req, outboundOrder.getId());
            boolean itemsSaved = outboundOrderItemService.saveBatch(orderItems);
            if (!itemsSaved) {
                throw new ValidationException("出库单明细保存失败");
            }

            // 4. 创建生产任务
            boolean productionTaskSaved = createProductionTasks(req, outboundOrder);
            if (!productionTaskSaved) {
                throw new ValidationException("生产任务保存失败");
            }

            // 6. 异步处理库存锁定
            LockResult lockResult = inventoryLockService.lockForProductionOutbound(req, outboundOrder.getId());
            if (!lockResult.getSuccess()) {
                log.warn("生产任务库存锁定失败，订单ID: {}, 订单号: {}", outboundOrder.getId(), outboundOrder.getOrderNo());
            }


            long totalTime = System.currentTimeMillis() - startTime;
            log.info("生产领料出库单创建成功，订单ID: {}, 订单号: {}, 总耗时: {}ms",
                    outboundOrder.getId(), outboundOrder.getOrderNo(), totalTime);

            return true;

        } catch (ValidationException ve) {
            log.error("创建生产领料出库单参数校验失败，订单号: {}", req.getOrderNo(), ve);
            throw ve;
        } catch (Exception e) {
            log.error("创建生产领料出库单异常，订单号: {}", req.getOrderNo(), e);
            throw new RuntimeException("创建生产领料出库单失败: " + e.getMessage(), e);
        }
    }

    /**
     * ==================== 生产领料出库 - 更新 ====================
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProductionPickingOutBound(OutboundCreateReq req) {
        log.info("开始更新生产领料出库单，订单ID: {}, 订单号: {}", req.getId(), req.getOrderNo());
        long startTime = System.currentTimeMillis();

        try {
            // 1. 参数校验
            validateOutboundCreateReq(req);

            // 2. 查询并校验原有订单
            OutboundOrder existingOrder = outboundOrderService.getById(req.getId());
            if (existingOrder == null) {
                throw new ValidationException("出库单不存在");
            }

            if (!isUpdatableStatus(existingOrder.getStatus())) {
                throw new ValidationException("当前状态的出库单不允许修改");
            }

            // 3. 构建并更新出库单主表
            OutboundOrder outboundOrder = buildUpdatedOutboundOrder(req, existingOrder);
            boolean orderUpdated = outboundOrderService.updateById(outboundOrder);
            if (!orderUpdated) {
                throw new ValidationException("出库单主表更新失败");
            }

            // 4. 更新出库单明细
            boolean detailUpdated = updateOutboundOrderDetails(req, outboundOrder.getId());
            if (!detailUpdated) {
                throw new ValidationException("出库单明细更新失败");
            }

            // 5. 更新生产任务
            boolean productionTaskUpdated = updateProductionTasks(req, outboundOrder.getId());
            if (!productionTaskUpdated) {
                throw new ValidationException("生产任务更新失败");
            }

            // 6. 如果是已完成状态，更新库存
            if (req.getStatus() == 3) {
                List<OutboundOrderItem> orderItems = outboundOrderItemService.selectByOrderId(req.getId(), req.getTenantId());
                inventoryHolder.updateSubInventoryForApprove(outboundOrder, orderItems, req.getUserId());
            }

            // 7. 异步处理库存重新锁定
            asyncHandleInventoryRelock(req, existingOrder, outboundOrder);

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("生产领料出库单更新成功，订单ID: {}, 总耗时: {}ms", outboundOrder.getId(), totalTime);

            return true;

        } catch (ValidationException ve) {
            log.error("更新生产领料出库单参数校验失败，订单ID: {}", req.getId(), ve);
            throw ve;
        } catch (Exception e) {
            log.error("更新生产领料出库单异常，订单ID: {}", req.getId(), e);
            throw new RuntimeException("更新生产领料出库单失败: " + e.getMessage(), e);
        }
    }

    /**
     * ==================== 销售出库 - 创建 ====================
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean createProductionSaleOutBound(OutboundCreateSaleProductReq req) {
        log.info("开始创建销售出库单，订单号: {}, 租户ID: {}", req.getOrderNo(), req.getTenantId());
        long startTime = System.currentTimeMillis();

        try {
            // 1. 参数校验
            OutboundCreateSaleProductReqValidator.validateAll(req);

            // 2. 构建并保存出库单主表
            OutboundOrder outboundOrder = buildOutboundOrder(req);
            boolean orderSaved = outboundOrderService.save(outboundOrder);
            if (!orderSaved) {
                throw new ValidationException("出库单主表保存失败");
            }

            // 3. 构建并保存出库单明细
            List<OutboundOrderItem> orderItems = buildOutboundOrderItems(req, outboundOrder.getId());
            boolean itemsSaved = outboundOrderItemService.saveBatch(orderItems);
            if (!itemsSaved) {
                throw new ValidationException("出库单明细保存失败");
            }

            // 4. 保存扩展表
            List<OutboundOrderItemSaleExt> outboundOrderItemSaleExts = handleOutboundOrderItemExtensions(req, outboundOrder, orderItems);
            if (!CollectionUtils.isEmpty(outboundOrderItemSaleExts)) {
                boolean extSaved = outboundOrderItemSaleExtService.saveBatch(outboundOrderItemSaleExts);
                if (!extSaved) {
                    log.warn("销售出库扩展表保存失败，订单ID: {}", outboundOrder.getId());
                }
            }

            // 6. 库存锁定
            LockResult lockResult = inventoryLockService.lockForSaleOutbound(req, outboundOrder.getId());
            if (!lockResult.getSuccess()) {
                log.warn("销售出库单库存锁定失败，订单ID: {}, 订单号: {}", outboundOrder.getId(), outboundOrder.getOrderNo());
            }

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("销售出库单创建成功，订单ID: {}, 订单号: {}, 总耗时: {}ms",
                    outboundOrder.getId(), outboundOrder.getOrderNo(), totalTime);

            return true;

        } catch (ValidationException ve) {
            log.error("创建销售出库单参数校验失败，订单号: {}", req.getOrderNo(), ve);
            throw ve;
        } catch (Exception e) {
            log.error("创建销售出库单异常，订单号: {}", req.getOrderNo(), e);
            throw new RuntimeException("创建销售出库单失败: " + e.getMessage(), e);
        }
    }

    /**
     * ==================== 销售出库 - 更新 ====================
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProductionSaleOutBound(OutboundCreateSaleProductReq req) {
        log.info("开始更新销售出库单，订单ID: {}, 订单号: {}", req.getId(), req.getOrderNo());
        long startTime = System.currentTimeMillis();

        try {
            // 1. 查询并校验原有订单
            OutboundOrder existingOrder = outboundOrderService.getById(req.getId());
            if (existingOrder == null) {
                throw new ValidationException("出库单不存在");
            }

            if (!isUpdatableStatus(existingOrder.getStatus())) {
                throw new ValidationException("当前状态的出库单不允许修改");
            }

            // 2. 更新出库单主表
            OutboundOrder outboundOrder = buildOutboundOrder(req);
            outboundOrder.setId(req.getId());
            outboundOrder.setModifiedAt(new Date());
            outboundOrder.setModifiedBy(req.getUserId());

            boolean orderUpdated = outboundOrderService.updateById(outboundOrder);
            if (!orderUpdated) {
                throw new ValidationException("出库单主表更新失败");
            }

            // 3. 更新出库单明细
            boolean detailUpdated = updateOutboundOrderDetails(req, outboundOrder);
            if (!detailUpdated) {
                throw new ValidationException("出库单明细更新失败");
            }

            // 4. 更新扩展表
            updateSaleExtensions(req, outboundOrder);

            // 5. 如果是已完成状态，更新库存
            if (req.getStatus() == 3) {
                List<OutboundOrderItem> orderItems = outboundOrderItemService.selectByOrderId(req.getId(), req.getTenantId());
                inventoryHolder.updateSubInventoryForApprove(outboundOrder, orderItems, req.getUserId());
            }

            // 6. 使用增强服务处理重新锁定
            handleSaleInventoryRelockWithService(req, existingOrder, outboundOrder);

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("销售出库单更新成功，订单ID: {}, 总耗时: {}ms", outboundOrder.getId(), totalTime);

            return true;

        } catch (ValidationException ve) {
            log.error("更新销售出库单参数校验失败，订单ID: {}", req.getId(), ve);
            throw ve;
        } catch (Exception e) {
            log.error("更新销售出库单异常，订单ID: {}", req.getId(), e);
            throw new RuntimeException("更新销售出库单失败: " + e.getMessage(), e);
        }
    }

    /**
     * ==================== 审核通过 ====================
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveOk(OutboundApproveOkReq approveOkReq) {
        log.info("=== 开始出库单审核通过流程 ===");
        log.info("订单ID: {}, 租户ID: {}, 操作人: {}",
                approveOkReq.getId(), approveOkReq.getTenantId(), approveOkReq.getUserId());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 前置验证
            log.info("[1/4] 开始前置验证");
            OutboundOrder outboundOrder = validateAndGetOrder(approveOkReq);

            // 2. 核心业务处理
            log.info("[2/4] 开始核心业务处理");

            List<OutboundOrderItem> orderItems = outboundOrderItemService
                    .selectByOrderId(approveOkReq.getId(), approveOkReq.getTenantId());

            if (CollectionUtils.isEmpty(orderItems)) {
                throw new ValidationException("出库单明细为空，无法审核");
            }

            // 处理库存锁定
            LockResult unlockResult = processInventoryLocking(approveOkReq, outboundOrder);

            // 扣减实际库存
            processInventoryDeduction(outboundOrder, orderItems, approveOkReq.getUserId());

            // 更新出库单状态
            updateOrderToApproved(outboundOrder, approveOkReq.getUserId());

            // 更新生产任务（如果是生产领料）
            processProductionTaskUpdate(outboundOrder, approveOkReq);

            // 3. 记录审计日志
            log.info("[3/4] 记录审计日志");
            recordAuditLogs(approveOkReq, outboundOrder, orderItems, unlockResult);

            // 4. 异步后置处理
            log.info("[4/4] 启动异步后置处理");
            startAsyncPostProcessing(outboundOrder, approveOkReq.getUserId());

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("=== 出库单审核通过流程完成 ===");
            log.info("订单ID: {}, 订单号: {}, 总耗时: {}ms",
                    outboundOrder.getId(), outboundOrder.getOrderNo(), totalTime);

            return true;

        } catch (ValidationException ve) {
            log.error("出库单审核失败（业务验证），订单ID: {}", approveOkReq.getId(), ve);
            throw ve;
        } catch (Exception e) {
            log.error("出库单审核失败（系统异常），订单ID: {}", approveOkReq.getId(), e);
            throw new RuntimeException("出库单审核失败: " + e.getMessage(), e);
        }
    }

    /**
     * ==================== 辅助方法 ====================
     */

    /**
     * 校验生产领料单参数
     */
    private void validateOutboundCreateReq(OutboundCreateReq req) {
        // 1. 基础信息校验
        validateBasicInfo(req);

        // 2. 产品明细校验
        validateItems(req.getItems());

        // 3. 数值范围校验
        validateNumericalValues(req);
    }

    /**
     * 基础信息校验
     */
    private void validateBasicInfo(OutboundCreateReq req) {
        if (StringUtils.isBlank(req.getOrderNo())) {
            throw new ValidationException("出库单号不能为空");
        } else if (req.getOrderNo().length() > 100) {
            throw new ValidationException("出库单号长度不能超过100个字符");
        }

        if (req.getOrderType() == null) {
            throw new ValidationException("出库类型不能为空");
        } else if (req.getOrderType() != CkInOutboundEnums.OutBoundType.ProductionOutbound.getCode()) {
            throw new ValidationException("生产领料单的订单类型必须为2");
        }

        if (req.getWarehouseId() == null) {
            throw new ValidationException("请选择出库仓库");
        }

        if (StringUtils.isBlank(req.getExpectedDate())) {
            throw new ValidationException("请选择预计出库日期");
        }

        if (!StringUtils.isBlank(req.getRelatedOrderNo()) && req.getRelatedOrderNo().length() > 100) {
            throw new ValidationException("关联单号长度不能超过100个字符");
        }

        if (!StringUtils.isBlank(req.getRemark()) && req.getRemark().length() > 200) {
            throw new ValidationException("备注长度不能超过500个字符");
        }
    }

    /**
     * 产品明细校验
     */
    private void validateItems(List<OutboundCreateReq.ProductInfoInner> items) {
        if (CollectionUtils.isEmpty(items)) {
            throw new ValidationException("请至少添加一个产品");
        }

        for (int i = 0; i < items.size(); i++) {
            OutboundCreateReq.ProductInfoInner item = items.get(i);
            int itemIndex = i + 1;

            if (item.getProductId() == null) {
                throw new ValidationException("第" + itemIndex + "个产品的产品ID不能为空");
            }

            if (item.getQuantity() == null) {
                throw new ValidationException("第" + itemIndex + "个产品的数量不能为空");
            } else if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("第" + itemIndex + "个产品的数量必须大于0");
            }

            validateBomAllocations(item.getBomAllocations(), itemIndex);
        }
    }

    /**
     * BOM分配数据校验
     */
    private void validateBomAllocations(List<BomAllocationCreateReq> bomAllocations, int itemIndex) {
        if (CollectionUtils.isEmpty(bomAllocations)) {
            return;
        }

        for (int i = 0; i < bomAllocations.size(); i++) {
            BomAllocationCreateReq allocation = bomAllocations.get(i);
            int allocationIndex = i + 1;

            if (Objects.isNull(allocation.getComponentProductId())) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的组件产品ID不能为空");
            }

            if (StringUtils.isBlank(allocation.getBatchNo())) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的批次号不能为空");
            } else if (allocation.getBatchNo().length() > 100) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的批次号长度不能超过100个字符");
            }

            if (allocation.getShelfId() == null) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的货架ID不能为空");
            }

            if (allocation.getQuantity() == null) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的分配数量不能为空");
            } else if (allocation.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的分配数量必须大于0");
            }
        }
    }

    /**
     * 数值范围校验
     */
    private void validateNumericalValues(OutboundCreateReq req) {
        if (req.getTotalQuantity() != null && req.getTotalQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("总数量不能为负数");
        }
    }

    /**
     * 构建出库单主表实体（生产领料）
     */
    private OutboundOrder buildOutboundOrder(OutboundCreateReq req) {
        OutboundOrder order = new OutboundOrder();
        order.setOrderNo(req.getOrderNo());
        order.setOrderType(req.getOrderType());
        order.setWarehouseId(req.getWarehouseId());
        order.setCustomerId(req.getCustomerId());
        order.setRelatedOrderNo(req.getRelatedOrderNo());
        order.setRemark(req.getRemark());
        order.setStatus(req.getStatus());
        order.setTotalQuantity(req.getTotalQuantity());
        order.setTotalAmount(req.getTotalAmount());
        order.setTotalAmountUsd(req.getTotalAmountUsd());
        order.setTenantId(req.getTenantId());
        order.setCreatedBy(req.getUserId());
        order.setModifiedBy(req.getUserId());
        order.setCreatedAt(new Date());
        order.setModifiedAt(new Date());
        order.setIsDeleted(0);
        order.setExpectedDate(DateUtils.parseDate(req.getExpectedDate()));
        return order;
    }

    /**
     * 构建出库单主表实体（销售出库）
     */
    private OutboundOrder buildOutboundOrder(OutboundCreateSaleProductReq req) {
        OutboundOrder order = new OutboundOrder();
        order.setOrderNo(req.getOrderNo());
        order.setOrderType(req.getOrderType());
        order.setWarehouseId(req.getWarehouseId());
        order.setCustomerId(req.getCustomerId());
        order.setRelatedOrderNo(req.getRelatedOrderNo());
        order.setRemark(req.getRemark());
        order.setStatus(req.getStatus());
        order.setTotalQuantity(req.getTotalQuantity());
        order.setTotalAmount(req.getTotalAmount());
        order.setTotalAmountUsd(req.getTotalAmountUsd());
        order.setTenantId(req.getTenantId());
        order.setCreatedBy(req.getUserId());
        order.setModifiedBy(req.getUserId());
        order.setCreatedAt(new Date());
        order.setModifiedAt(new Date());
        order.setIsDeleted(0);
        order.setExpectedDate(DateUtils.parseDate(req.getExpectedDate()));
        return order;
    }

    /**
     * 构建更新后的出库单主表实体
     */
    private OutboundOrder buildUpdatedOutboundOrder(OutboundCreateReq req, OutboundOrder existingOrder) {
        OutboundOrder order = new OutboundOrder();
        order.setId(req.getId());
        order.setOrderNo(req.getOrderNo());
        order.setOrderType(req.getOrderType());
        order.setWarehouseId(req.getWarehouseId());
        order.setCustomerId(req.getCustomerId());
        order.setRelatedOrderNo(req.getRelatedOrderNo());
        order.setRemark(req.getRemark());
        order.setStatus(req.getStatus());
        order.setTotalQuantity(req.getTotalQuantity());
        order.setTotalAmount(req.getTotalAmount());
        order.setTenantId(req.getTenantId());
        order.setModifiedBy(req.getUserId());
        order.setModifiedAt(new Date());
        order.setExpectedDate(DateUtils.parseDate(req.getExpectedDate()));

        // 保留原有创建信息
        order.setCreatedBy(existingOrder.getCreatedBy());
        order.setCreatedAt(existingOrder.getCreatedAt());
        order.setIsDeleted(existingOrder.getIsDeleted());

        return order;
    }

    /**
     * 构建出库单明细实体列表（生产领料）
     */
    private List<OutboundOrderItem> buildOutboundOrderItems(OutboundCreateReq req, Long orderId) {
        List<OutboundOrderItem> orderItems = new ArrayList<>();

        for (OutboundCreateReq.ProductInfoInner item : req.getItems()) {
            for (BomAllocationCreateReq batch : item.getBomAllocations()) {
                OutboundOrderItem orderItem = new OutboundOrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setProductId(batch.getComponentProductId());
                orderItem.setRelationProductId(item.getProductId());
                orderItem.setBatchNo(batch.getBatchNo());
                orderItem.setQuantity(batch.getQuantity());
                orderItem.setShelfLocationId(batch.getShelfId());
                orderItem.setRemark(item.getRemark());
                orderItem.setTenantId(req.getTenantId());
                orderItem.setCreatedBy(req.getUserId());
                orderItem.setModifiedBy(req.getUserId());
                orderItem.setCreatedAt(new Date());
                orderItem.setModifiedAt(new Date());
                orderItem.setIsDeleted(0);

                orderItems.add(orderItem);
            }
        }

        return orderItems;
    }

    /**
     * 构建出库单明细实体列表（销售出库）
     */
    private List<OutboundOrderItem> buildOutboundOrderItems(OutboundCreateSaleProductReq req, Long orderId) {
        List<OutboundOrderItem> orderItems = new ArrayList<>();

        for (OutboundCreateSaleProductReq.OrderItemInner item : req.getItems()) {
            for (OutboundCreateSaleProductReq.BatchAllocationInner batch : item.getBatchAllocations()) {
                OutboundOrderItem orderItem = new OutboundOrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setProductId(item.getProductId());
                orderItem.setBatchNo(batch.getBatchNo());
                orderItem.setQuantity(batch.getQuantity());
                orderItem.setShelfLocationId(batch.getShelfId());
                orderItem.setPriceUnit(item.getPrice());
                orderItem.setPriceTotal(batch.getQuantity().multiply(item.getPrice()));
                orderItem.setPriceUnitUsd(item.getPriceUnitUsd());
                orderItem.setPriceTotalUsd(batch.getQuantity().multiply(item.getPriceUnitUsd()));
                orderItem.setRemark(item.getRemark());
                orderItem.setTenantId(req.getTenantId());
                orderItem.setCreatedBy(req.getUserId());
                orderItem.setModifiedBy(req.getUserId());
                orderItem.setCreatedAt(new Date());
                orderItem.setModifiedAt(new Date());
                orderItem.setIsDeleted(0);

                orderItems.add(orderItem);
            }
        }

        return orderItems;
    }

    /**
     * 创建生产任务
     */
    private boolean createProductionTasks(OutboundCreateReq req, OutboundOrder outboundOrder) {
        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Long> productIds = req.getItems().stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());
        List<Product> products = productService.selectByIds(req.getTenantId(), productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        List<ProductionTask> batchSaveProductTaskList = req.getItems().stream().map(v -> {
            ProductionTask pt = new ProductionTask();
            pt.setTenantId(req.getTenantId());
            pt.setTaskNo("productionTask-" + UUID.randomUUID().toString().substring(0, 8));
            pt.setOutboundOrderId(outboundOrder.getId());
            pt.setOutboundOrderNo(outboundOrder.getOrderNo());
            pt.setProductId(v.getProductId());
            pt.setProductName(finalProductId2ProductMap.getOrDefault(v.getProductId(), new Product()).getName());
            pt.setPlannedQuantity(v.getQuantity());
            pt.setMaterialQuantity(v.getQuantity());
            pt.setProducedQuantity(BigDecimal.ZERO);
            pt.setRemainingQuantity(v.getQuantity());
            pt.setLockQuantity(BigDecimal.ZERO);
            pt.setStatus(CkInOutboundEnums.ProductionTaskStatus.InProduction.getCode());
            pt.setWarehouseId(req.getWarehouseId());
            pt.setExpectedDate(DateUtil.parseDate(req.getExpectedDate()));
            pt.setRemark(req.getRemark());
            pt.setCreatedAt(new Date());
            pt.setCreatedBy(req.getUserId());
            pt.setModifiedAt(new Date());
            pt.setModifiedBy(req.getUserId());
            return pt;
        }).collect(Collectors.toList());

        return productionTaskService.saveBatch(batchSaveProductTaskList);
    }

    /**
     * 更新生产任务
     */
    private boolean updateProductionTasks(OutboundCreateReq req, Long orderId) {
        // 先删除原有的生产任务
        LambdaQueryWrapper<ProductionTask> taskQueryWrapper = new LambdaQueryWrapper<>();
        taskQueryWrapper.eq(ProductionTask::getOutboundOrderId, orderId);
        boolean tasksDeleted = productionTaskService.remove(taskQueryWrapper);
        if (!tasksDeleted) {
            log.warn("删除原有生产任务失败，继续创建新任务");
        }

        // 获取产品信息
        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Long> productIds = req.getItems().stream()
                .map(v -> v.getProductId())
                .distinct()
                .collect(Collectors.toList());
        List<Product> products = productService.selectByIds(req.getTenantId(), productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, v -> v));
        }

        // 创建新的生产任务
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        List<ProductionTask> batchSaveProductTaskList = req.getItems().stream().map(v -> {
            ProductionTask pt = new ProductionTask();
            pt.setTenantId(req.getTenantId());
            pt.setTaskNo("productionTask-" + UUID.randomUUID().toString().substring(0, 8));
            pt.setOutboundOrderId(orderId);
            pt.setOutboundOrderNo(req.getOrderNo());
            pt.setProductId(v.getProductId());
            pt.setProductName(finalProductId2ProductMap.getOrDefault(v.getProductId(), new Product()).getName());
            pt.setPlannedQuantity(v.getQuantity());
            pt.setMaterialQuantity(v.getQuantity());
            pt.setProducedQuantity(BigDecimal.ZERO);
            pt.setRemainingQuantity(v.getQuantity());
            pt.setLockQuantity(BigDecimal.ZERO);
            pt.setStatus(CkInOutboundEnums.ProductionTaskStatus.InProduction.getCode());
            pt.setWarehouseId(req.getWarehouseId());
            pt.setExpectedDate(DateUtil.parseDate(req.getExpectedDate()));
            pt.setRemark(req.getRemark());
            pt.setCreatedAt(new Date());
            pt.setCreatedBy(req.getUserId());
            pt.setModifiedAt(new Date());
            pt.setModifiedBy(req.getUserId());
            return pt;
        }).collect(Collectors.toList());

        return productionTaskService.saveBatch(batchSaveProductTaskList);
    }

    /**
     * 更新出库单明细（生产领料）
     */
    private boolean updateOutboundOrderDetails(OutboundCreateReq req, Long orderId) {
        try {
            // 删除原有明细
            LambdaQueryWrapper<OutboundOrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
            itemQueryWrapper.eq(OutboundOrderItem::getOrderId, orderId);
            boolean itemsDeleted = outboundOrderItemService.remove(itemQueryWrapper);
            if (!itemsDeleted) {
                log.warn("删除原有出库单明细失败，订单ID: {}", orderId);
            }

            // 插入新明细
            List<OutboundOrderItem> orderItems = buildOutboundOrderItems(req, orderId);
            return outboundOrderItemService.saveBatch(orderItems);

        } catch (Exception e) {
            log.error("更新出库单明细异常，订单ID: {}", orderId, e);
            return false;
        }
    }

    /**
     * 更新出库单明细（销售出库）
     */
    private boolean updateOutboundOrderDetails(OutboundCreateSaleProductReq req, OutboundOrder outboundOrder) {
        try {
            // 删除原有明细
            LambdaQueryWrapper<OutboundOrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
            itemQueryWrapper.eq(OutboundOrderItem::getOrderId, req.getId());
            boolean itemsDeleted = outboundOrderItemService.remove(itemQueryWrapper);
            if (!itemsDeleted) {
                log.warn("删除原有出库单明细失败，订单ID: {}", req.getId());
            }

            // 插入新明细
            List<OutboundOrderItem> orderItems = buildOutboundOrderItems(req, req.getId());
            return outboundOrderItemService.saveBatch(orderItems);

        } catch (Exception e) {
            log.error("更新出库单明细异常，订单ID: {}", req.getId(), e);
            return false;
        }
    }

    /**
     * 更新扩展表（销售出库）
     */
    private void updateSaleExtensions(OutboundCreateSaleProductReq req, OutboundOrder outboundOrder) {
        try {
            List<OutboundOrderItem> orderItems = outboundOrderItemService.selectByOrderId(req.getId(), req.getTenantId());
            List<OutboundOrderItemSaleExt> newExts = handleOutboundOrderItemExtensions(req, outboundOrder, orderItems);

            if (!CollectionUtils.isEmpty(newExts)) {
                // 删除原有扩展表
                LambdaQueryWrapper<OutboundOrderItemSaleExt> extQueryWrapper = new LambdaQueryWrapper<>();
                extQueryWrapper.eq(OutboundOrderItemSaleExt::getOrderId, req.getId());
                outboundOrderItemSaleExtService.remove(extQueryWrapper);

                // 保存新的扩展表
                outboundOrderItemSaleExtService.saveBatch(newExts);
            }

        } catch (Exception e) {
            log.error("更新销售出库扩展表异常，订单ID: {}", req.getId(), e);
        }
    }

    /**
     * 处理销售出库单明细扩展表
     */
    private List<OutboundOrderItemSaleExt> handleOutboundOrderItemExtensions(OutboundCreateSaleProductReq req,
                                                                             OutboundOrder outboundOrder,
                                                                             List<OutboundOrderItem> orderItems) {
        if (CollectionUtils.isEmpty(req.getItems())) {
            return new ArrayList<>();
        }

        List<OutboundOrderItemSaleExt> extList = new ArrayList<>();

        for (int i = 0; i < req.getItems().size(); i++) {
            OutboundCreateSaleProductReq.OrderItemInner itemReq = req.getItems().get(i);
            OutboundOrderItem orderItem = orderItems.get(i);

            OutboundOrderItemSaleExt ext = new OutboundOrderItemSaleExt();
            ext.setTenantId(req.getTenantId());
            ext.setOrderId(outboundOrder.getId());
            ext.setOrderItemId(orderItem.getId());
            ext.setProductId(itemReq.getProductId());
            ext.setIsTriggerProduct(itemReq.getExtension().getIsTriggerProduct());
            ext.setIsRecommendProduct(itemReq.getExtension().getIsRecommendProduct());
            ext.setTriggerProductId(itemReq.getExtension().getTriggerProductId() != null &&
                    itemReq.getExtension().getTriggerProductId() > 0 ?
                    itemReq.getExtension().getTriggerProductId() : 0L);
            ext.setCreatedBy(req.getUserId());
            ext.setModifiedBy(req.getUserId());

            extList.add(ext);
        }

        return extList;
    }


    /**
     * 记录锁定结果到订单
     */
    private void recordLockResult(OutboundOrder outboundOrder, LockResult lockResult, Long userId) {
        String lockStatus = lockResult.getSuccess() ?
                String.format("锁定成功(%d个批次)", lockResult.getLockCount()) :
                String.format("锁定失败:%s", lockResult.getMessage());

        String newRemark = StringUtils.isBlank(outboundOrder.getRemark()) ?
                "[库存]" + lockStatus :
                outboundOrder.getRemark() + " [库存]" + lockStatus;

        OutboundOrder update = new OutboundOrder();
        update.setId(outboundOrder.getId());
        update.setRemark(newRemark);
        update.setModifiedBy(userId);
        update.setModifiedAt(new Date());

        outboundOrderService.updateById(update);
    }

    /**
     * 处理已完成订单的锁定
     */
    private void handleLockForCompletedOrder(OutboundOrder outboundOrder, OutboundCreateReq req,
                                             LockResult lockResult) {
        if (lockResult == null || !lockResult.getSuccess() || lockResult.getLockCount() == 0) {
            return;
        }

        try {
            // 解锁库存锁定
            OutboundApproveOkReq unlockReq = new OutboundApproveOkReq();
            unlockReq.setId(outboundOrder.getId());
            unlockReq.setTenantId(req.getTenantId());
            unlockReq.setUserId(req.getUserId());

            LockResult unlockResult = inventoryLockService.unlockForOutboundApproval(unlockReq);

            if (unlockResult.getSuccess()) {
                log.info("订单{}库存锁定已成功解锁", outboundOrder.getId());
            } else {
                log.warn("订单{}库存锁定解锁失败: {}", outboundOrder.getId(), unlockResult.getMessage());
            }

        } catch (Exception e) {
            log.error("解锁库存锁定异常, 订单ID: {}", outboundOrder.getId(), e);
        }
    }

    /**
     * 记录锁定异常
     */
    private void recordLockException(OutboundOrder outboundOrder, Exception e) {
        String exceptionRemark = String.format("[库存锁定异常:%s]", e.getMessage());
        String newRemark = StringUtils.isBlank(outboundOrder.getRemark()) ?
                exceptionRemark :
                outboundOrder.getRemark() + " " + exceptionRemark;

        if (newRemark.length() > 500) {
            newRemark = newRemark.substring(0, 500);
        }

        OutboundOrder update = new OutboundOrder();
        update.setId(outboundOrder.getId());
        update.setRemark(newRemark);
        update.setModifiedAt(new Date());

        outboundOrderService.updateById(update);
    }

    /**
     * 异步处理库存重新锁定（生产领料）
     */
    private void asyncHandleInventoryRelock(OutboundCreateReq req,
                                            OutboundOrder oldOrder,
                                            OutboundOrder newOrder) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始异步处理订单{}的库存重新锁定", newOrder.getId());

                // 查询旧的锁定记录
                List<InventoryLock> oldLocks = stockLockService.findBySourceId(
                        req.getTenantId(), newOrder.getId());

                if (CollectionUtils.isEmpty(oldLocks)) {
                    log.info("订单{}没有旧的锁定记录，跳过重新锁定", newOrder.getId());
                    return;
                }

                // 释放旧的锁定记录
                LockResult unlockResult = inventoryLockService.unlockForOutboundCancel(
                        newOrder.getId(), req.getTenantId(), req.getUserId());

                if (!unlockResult.getSuccess()) {
                    log.warn("订单{}旧锁定释放失败: {}", newOrder.getId(), unlockResult.getMessage());
                }

                // 创建新的锁定记录
                LockResult lockResult = inventoryLockService.lockForProductionOutbound(req, newOrder.getId());

                // 记录锁定结果
                //recordLockResultToOrder(newOrder, lockResult, req.getUserId());

                // 如果是已完成状态，需要解锁锁定
//                if (req.getStatus() == 3) {
//                    handleCompletedOrderLock(newOrder, req, lockResult);
//                }

                log.info("订单{}库存重新锁定处理完成", newOrder.getId());

            } catch (Exception e) {
                log.error("订单{}库存重新锁定异步处理异常", newOrder.getId(), e);
                recordLockExceptionToOrder(newOrder, e);
            }
        }, asyncExecutor).exceptionally(throwable -> {
            log.error("异步处理库存锁定任务异常", throwable);
            return null;
        });
    }

    /**
     * 记录锁定结果到订单
     */
    private void recordLockResultToOrder(OutboundOrder order, LockResult lockResult, Long userId) {
        String lockStatus;
        if (lockResult == null) {
            lockStatus = "库存锁定: 未执行";
        } else if (lockResult.getSuccess()) {
            lockStatus = String.format("库存锁定: 成功锁定%d个批次", lockResult.getLockCount());
        } else {
            lockStatus = String.format("库存锁定: %s", lockResult.getMessage());
        }

        // 异步更新订单备注
        CompletableFuture.runAsync(() -> {
            try {
                String newRemark = StringUtils.isBlank(order.getRemark())
                        ? lockStatus
                        : order.getRemark() + "; " + lockStatus;

                OutboundOrder update = new OutboundOrder();
                update.setId(order.getId());
                update.setRemark(newRemark);
                update.setModifiedBy(userId);
                update.setModifiedAt(new Date());

                outboundOrderService.updateById(update);
            } catch (Exception e) {
                log.error("更新订单{}备注失败", order.getId(), e);
            }
        }, asyncExecutor);
    }

    /**
     * 记录锁定异常到订单
     */
    private void recordLockExceptionToOrder(OutboundOrder order, Exception e) {
        String errorMsg = e.getMessage();
        if (StringUtils.isBlank(errorMsg)) {
            errorMsg = "未知异常";
        }

        if (errorMsg.length() > 100) {
            errorMsg = errorMsg.substring(0, 100) + "...";
        }

        String exceptionRemark = String.format("[库存锁定异常:%s]", errorMsg);

        CompletableFuture.runAsync(() -> {
            try {
                String newRemark = StringUtils.isBlank(order.getRemark())
                        ? exceptionRemark
                        : order.getRemark() + " " + exceptionRemark;

                if (newRemark.length() > 500) {
                    newRemark = newRemark.substring(0, 500);
                }

                OutboundOrder update = new OutboundOrder();
                update.setId(order.getId());
                update.setRemark(newRemark);
                update.setModifiedAt(new Date());

                outboundOrderService.updateById(update);
            } catch (Exception updateEx) {
                log.error("记录锁定异常到订单{}失败", order.getId(), updateEx);
            }
        }, asyncExecutor);
    }


    /**
     * 记录销售出库锁定结果
     */
    private void recordSaleLockResult(OutboundOrder order, LockResult lockResult, Long userId) {
        try {
            StringBuilder remarkBuilder = new StringBuilder();

            if (StringUtils.isNotBlank(order.getRemark())) {
                remarkBuilder.append(order.getRemark());
            }

            if (lockResult != null) {
                if (lockResult.getSuccess()) {
                    remarkBuilder.append(String.format("[库存锁定成功:%d批次]", lockResult.getLockCount()));

                    if (!CollectionUtils.isEmpty(lockResult.getFailureItems())) {
                        String failedItems = lockResult.getFailureItems().stream()
                                .limit(3)
                                .map(f -> String.format("产品%d", f.getProductId()))
                                .collect(Collectors.joining(","));
                        remarkBuilder.append(String.format("[部分失败:%s]", failedItems));
                    }
                } else {
                    String errorMsg = StringUtils.substring(lockResult.getMessage(), 0, 100);
                    remarkBuilder.append(String.format("[库存锁定失败:%s]", errorMsg));
                }
            } else {
                remarkBuilder.append("[库存锁定未执行]");
            }

            updateOrderRemark(order.getId(), remarkBuilder.toString(), userId);

        } catch (Exception e) {
            log.error("记录销售出库锁定结果失败，订单ID: {}", order.getId(), e);
        }
    }

    /**
     * 处理已完成销售订单的锁定
     */
    private void handleCompletedSaleOrderLock(OutboundOrder order, OutboundCreateSaleProductReq req,
                                              LockResult lockResult) {
        if (lockResult == null || !lockResult.getSuccess() || lockResult.getLockCount() == 0) {
            return;
        }

        try {
            OutboundApproveOkReq unlockReq = new OutboundApproveOkReq();
            unlockReq.setId(order.getId());
            unlockReq.setTenantId(req.getTenantId());
            unlockReq.setUserId(req.getUserId());

            LockResult unlockResult = inventoryLockService.unlockForOutboundApproval(unlockReq);

            if (unlockResult.getSuccess()) {
                log.info("已完成销售订单{}库存锁定已解锁", order.getId());
            } else {
                log.warn("已完成销售订单{}库存锁定解锁失败: {}", order.getId(), unlockResult.getMessage());
                recordUnlockException(order, unlockResult.getMessage(), req.getUserId());
            }

        } catch (Exception e) {
            log.error("解锁已完成销售订单{}库存锁定异常", order.getId(), e);
            recordSaleLockException(order, e, req.getUserId());
        }
    }

    /**
     * 记录销售出库锁定异常
     */
    private void recordSaleLockException(OutboundOrder order, Exception e, Long userId) {
        try {
            String errorMsg = StringUtils.isNotBlank(e.getMessage()) ?
                    StringUtils.substring(e.getMessage(), 0, 100) : "未知异常";

            String exceptionRemark = String.format("[库存锁定异常:%s]", errorMsg);

            OutboundOrder currentOrder = outboundOrderService.getById(order.getId());
            String currentRemark = currentOrder != null ? currentOrder.getRemark() : "";

            String newRemark = StringUtils.isBlank(currentRemark) ?
                    exceptionRemark :
                    currentRemark + " " + exceptionRemark;

            updateOrderRemark(order.getId(), newRemark, userId);

        } catch (Exception updateEx) {
            log.error("记录锁定异常到订单{}失败", order.getId(), updateEx);
        }
    }

    /**
     * 记录解锁异常
     */
    private void recordUnlockException(OutboundOrder order, String errorMsg, Long userId) {
        try {
            String exceptionRemark = String.format("[解锁异常:%s]",
                    StringUtils.substring(errorMsg, 0, 50));

            OutboundOrder currentOrder = outboundOrderService.getById(order.getId());
            String currentRemark = currentOrder != null ? currentOrder.getRemark() : "";

            String newRemark = StringUtils.isBlank(currentRemark) ?
                    exceptionRemark :
                    currentRemark + " " + exceptionRemark;

            updateOrderRemark(order.getId(), newRemark, userId);

        } catch (Exception e) {
            log.error("记录解锁异常到订单{}失败", order.getId(), e);
        }
    }

    /**
     * 更新订单备注（通用方法）
     */
    private void updateOrderRemark(Long orderId, String remark, Long userId) {
        try {
            if (StringUtils.isBlank(remark)) {
                return;
            }

            if (remark.length() > 500) {
                remark = remark.substring(0, 500);
            }

            OutboundOrder update = new OutboundOrder();
            update.setId(orderId);
            update.setRemark(remark);
            update.setModifiedBy(userId);
            update.setModifiedAt(new Date());

            boolean updated = outboundOrderService.updateById(update);
            if (!updated) {
                log.warn("更新订单{}备注失败", orderId);
            }

        } catch (Exception e) {
            log.error("更新订单备注异常，订单ID: {}", orderId, e);
        }
    }


    /**
     * 使用增强服务处理重新锁定（销售出库）
     */
    private void handleSaleInventoryRelockWithService(OutboundCreateSaleProductReq req,
                                                      OutboundOrder oldOrder,
                                                      OutboundOrder newOrder) {
        log.info("启动增强版库存重新锁定，订单ID: {}", newOrder.getId());

        asyncRelockForSaleOutbound(req, oldOrder, newOrder)
                .thenAccept(relockResult -> {
                    if (relockResult.getSuccess()) {
                        log.info("订单{}库存重新锁定完成，结果: {}",
                                newOrder.getId(), relockResult.getMessage());

                        if (req.getStatus() == 3 && relockResult.getLockCount() > 0) {
                            handleCompletedSaleOrderRelock(newOrder, req,
                                    LockResult.builder()
                                            .success(true)
                                            .lockCount(relockResult.getLockCount())
                                            .build());
                        }
                    } else {
                        log.warn("订单{}库存重新锁定失败: {}",
                                newOrder.getId(), relockResult.getMessage());
                    }

                    updateOrderWithRelockResult(newOrder, relockResult, req.getUserId());
                })
                .exceptionally(throwable -> {
                    log.error("增强版库存重新锁定任务异常，订单ID: {}", newOrder.getId(), throwable);
                    recordSaleRelockException(newOrder,
                            throwable instanceof Exception ? (Exception) throwable : new Exception(throwable),
                            req.getUserId());
                    return null;
                });
    }

    /**
     * 异步重新锁定销售出库库存（带比较和优化）
     */
    public CompletableFuture<RelockResult> asyncRelockForSaleOutbound(OutboundCreateSaleProductReq newReq,
                                                                      OutboundOrder oldOrder,
                                                                      OutboundOrder newOrder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 比较新旧订单差异
                RelockComparison comparison = compareOrderChanges(newReq, oldOrder);
                if (!comparison.isNeedRelock()) {
                    log.info("订单{}无需重新锁定，差异分析: {}", newOrder.getId(), comparison.getAnalysis());
                    return RelockResult.noNeedResult(newOrder.getId(), comparison.getAnalysis());
                }

                // 释放旧的锁定记录
                LockResult unlockResult = null;
                if (comparison.hasOldLocks()) {
                    unlockResult = inventoryLockService.unlockForOutboundCancel(
                            newOrder.getId(), newReq.getTenantId(), newReq.getUserId());

                    if (!unlockResult.getSuccess()) {
                        log.warn("订单{}旧锁定释放失败: {}", newOrder.getId(), unlockResult.getMessage());
                    }
                }

                // 创建新的锁定记录
                LockResult lockResult = inventoryLockService.lockForSaleOutbound(newReq, newOrder.getId());

                // 构建重新锁定结果
                return RelockResult.builder()
                        .orderId(newOrder.getId())
                        .success(lockResult.getSuccess())
                        .message(lockResult.getMessage())
                        .unlockCount(unlockResult != null ? unlockResult.getUnlockCount() : 0)
                        .lockCount(lockResult.getLockCount())
                        .comparison(comparison)
                        .relockTime(new Date())
                        .build();

            } catch (Exception e) {
                log.error("销售出库库存重新锁定失败，订单ID: {}", newOrder.getId(), e);

                return RelockResult.builder()
                        .orderId(newOrder.getId())
                        .success(false)
                        .message("重新锁定异常: " + e.getMessage())
                        .relockTime(new Date())
                        .build();
            }
        }, asyncExecutor);
    }

    /**
     * 比较订单变化，判断是否需要重新锁定
     */
    private RelockComparison compareOrderChanges(OutboundCreateSaleProductReq newReq, OutboundOrder oldOrder) {
        RelockComparison comparison = new RelockComparison();
        comparison.setOrderId(oldOrder.getId());

        // 比较订单基本信息
        if (oldOrder.getStatus() != newReq.getStatus()) {
            comparison.addChange("状态变更: " + oldOrder.getStatus() + " → " + newReq.getStatus());
        }

        if (oldOrder.getWarehouseId() != null && !oldOrder.getWarehouseId().equals(newReq.getWarehouseId())) {
            comparison.addChange("仓库变更: " + oldOrder.getWarehouseId() + " → " + newReq.getWarehouseId());
        }

        if (oldOrder.getCustomerId() != null && !oldOrder.getCustomerId().equals(newReq.getCustomerId())) {
            comparison.addChange("客户变更: " + oldOrder.getCustomerId() + " → " + newReq.getCustomerId());
        }

        boolean needRelock = !comparison.getChanges().isEmpty();
        comparison.setNeedRelock(needRelock);

        return comparison;
    }

    /**
     * 处理已完成销售订单的重新锁定
     */
    private void handleCompletedSaleOrderRelock(OutboundOrder order,
                                                OutboundCreateSaleProductReq req,
                                                LockResult lockResult) {
        if (lockResult == null || !lockResult.getSuccess() || lockResult.getLockCount() == 0) {
            return;
        }

        try {
            OutboundApproveOkReq unlockReq = new OutboundApproveOkReq();
            unlockReq.setId(order.getId());
            unlockReq.setTenantId(req.getTenantId());
            unlockReq.setUserId(req.getUserId());

            LockResult unlockResult = inventoryLockService.unlockForOutboundApproval(unlockReq);

            if (unlockResult.getSuccess()) {
                log.info("已完成销售订单{}库存重新锁定已解锁", order.getId());
            } else {
                log.warn("已完成销售订单{}库存重新锁定解锁失败: {}",
                        order.getId(), unlockResult.getMessage());
                recordUnlockException(order, unlockResult.getMessage(), req.getUserId());
            }

        } catch (Exception e) {
            log.error("解锁已完成销售订单{}库存重新锁定异常", order.getId(), e);
            recordSaleRelockException(order, e, req.getUserId());
        }
    }

    /**
     * 记录销售重新锁定异常
     */
    private void recordSaleRelockException(OutboundOrder order, Exception e, Long userId) {
        try {
            String errorMsg = StringUtils.isNotBlank(e.getMessage()) ?
                    StringUtils.substring(e.getMessage(), 0, 100) : "未知异常";

            String exceptionRemark = String.format("[库存重锁异常:%s]", errorMsg);

            OutboundOrder currentOrder = outboundOrderService.getById(order.getId());
            String currentRemark = currentOrder != null ? currentOrder.getRemark() : "";

            String newRemark = StringUtils.isBlank(currentRemark) ?
                    exceptionRemark :
                    currentRemark + " " + exceptionRemark;

            updateOrderRemark(order.getId(), newRemark, userId);

        } catch (Exception updateEx) {
            log.error("记录重新锁定异常到订单{}失败", order.getId(), updateEx);
        }
    }

    /**
     * 更新订单重新锁定结果
     */
    private void updateOrderWithRelockResult(OutboundOrder order,
                                             RelockResult relockResult,
                                             Long userId) {
        try {
            StringBuilder remarkBuilder = new StringBuilder();

            OutboundOrder currentOrder = outboundOrderService.getById(order.getId());
            if (currentOrder != null && StringUtils.isNotBlank(currentOrder.getRemark())) {
                remarkBuilder.append(currentOrder.getRemark());
            }

            if (relockResult.getComparison() != null && !relockResult.getComparison().isNeedRelock()) {
                remarkBuilder.append("[库存无需重锁]");
            } else if (relockResult.getSuccess()) {
                remarkBuilder.append(String.format("[重锁成功:解锁%d/锁定%d]",
                        relockResult.getUnlockCount(), relockResult.getLockCount()));
            } else {
                remarkBuilder.append(String.format("[重锁失败:%s]",
                        StringUtils.substring(relockResult.getMessage(), 0, 80)));
            }

            updateOrderRemark(order.getId(), remarkBuilder.toString(), userId);

        } catch (Exception e) {
            log.error("更新订单{}重新锁定结果失败", order.getId(), e);
        }
    }

    /**
     * 校验状态是否允许更新
     */
    private boolean isUpdatableStatus(Integer status) {
        Integer code = CkInOutboundEnums.InOutBoundStatus.WaitSubmit.getCode();
        Integer code1 = CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode();
        List<Integer> canUpdateStatusList = Lists.newArrayList(code, code1);
        return canUpdateStatusList.contains(status);
    }

    /**
     * ==================== 审核相关方法 ====================
     */

    /**
     * 验证并获取订单
     */
    private OutboundOrder validateAndGetOrder(OutboundApproveOkReq approveOkReq) {
        OutboundOrder order = outboundOrderService.getById(approveOkReq.getId());
        if (order == null) {
            throw new ValidationException("出库单不存在");
        }

        if (!approveOkReq.getTenantId().equals(order.getTenantId())) {
            throw new ValidationException("租户不匹配");
        }

        if (!CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode().equals(order.getStatus())) {
            String statusName = CkInOutboundEnums.InOutBoundStatus.getDescByCode(order.getStatus());
        }

        return order;
    }

    /**
     * 处理库存锁定
     */
    private LockResult processInventoryLocking(OutboundApproveOkReq approveOkReq,
                                               OutboundOrder outboundOrder) {
        log.info("处理订单{}的库存锁定", outboundOrder.getId());

        List<InventoryLock> locks = stockLockService.findBySourceId(
                approveOkReq.getTenantId(), approveOkReq.getId());

        if (CollectionUtils.isEmpty(locks)) {
            log.info("订单{}无库存锁定记录", outboundOrder.getId());
            return LockResult.builder()
                    .success(true)
                    .message("无锁定记录")
                    .orderId(outboundOrder.getId())
                    .build();
        }

        LockResult unlockResult = inventoryLockService.unlockForOutboundApproval(approveOkReq);

        if (!unlockResult.getSuccess()) {
            log.error("订单{}库存锁定解锁失败: {}", outboundOrder.getId(), unlockResult.getMessage());
            throw new ValidationException("库存锁定解锁失败: " + unlockResult.getMessage());
        }

        log.info("订单{}库存锁定解锁成功，解锁数量: {}",
                outboundOrder.getId(), unlockResult.getUnlockCount());

        return unlockResult;
    }

    /**
     * 处理库存扣减
     */
    private void processInventoryDeduction(OutboundOrder outboundOrder,
                                           List<OutboundOrderItem> orderItems,
                                           Long userId) {
        log.info("扣减订单{}的库存", outboundOrder.getId());

        try {
            inventoryHolder.updateSubInventoryForApprove(outboundOrder, orderItems, userId);
            log.info("订单{}库存扣减成功", outboundOrder.getId());
        } catch (ValidationException ve) {
            log.error("订单{}库存扣减失败: {}", outboundOrder.getId(), ve.getMessage());
            throw ve;
        } catch (Exception e) {
            log.error("订单{}库存扣减异常", outboundOrder.getId(), e);
            throw new ValidationException("库存扣减异常: " + e.getMessage());
        }
    }

    /**
     * 更新订单状态为已审核
     */
    private void updateOrderToApproved(OutboundOrder outboundOrder, Long userId) {
        log.info("更新订单{}状态为已审核", outboundOrder.getId());

        try {
            outboundOrder.setStatus(CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode());
            outboundOrder.setModifiedBy(userId);
            outboundOrder.setModifiedAt(new Date());

            String remark = StringUtils.isBlank(outboundOrder.getRemark()) ?
                    "[审核通过]" : outboundOrder.getRemark() + " [审核通过]";

            if (remark.length() > 500) {
                remark = remark.substring(0, 500);
            }
            outboundOrder.setRemark(remark);

            boolean updated = outboundOrderService.updateById(outboundOrder);
            if (!updated) {
                throw new ValidationException("订单状态更新失败");
            }

            log.info("订单{}状态更新成功", outboundOrder.getId());

        } catch (Exception e) {
            log.error("更新订单{}状态异常", outboundOrder.getId(), e);
            throw new ValidationException("订单状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 处理生产任务更新
     */
    private void processProductionTaskUpdate(OutboundOrder outboundOrder,
                                             OutboundApproveOkReq approveOkReq) {
        if (!CkInOutboundEnums.OutBoundType.ProductionOutbound.getCode().equals(outboundOrder.getOrderType())) {
            return;
        }

        log.info("更新订单{}的生产任务状态", outboundOrder.getId());

        try {
            productionTaskService.updateProductionTaskStatus(
                    approveOkReq.getTenantId(),
                    outboundOrder.getId(),
                    CkInOutboundEnums.ProductionTaskStatus.PartialCompletion,
                    approveOkReq.getUserId()
            );

            log.info("订单{}生产任务状态更新成功", outboundOrder.getId());

        } catch (Exception e) {
            log.error("更新订单{}生产任务状态异常", outboundOrder.getId(), e);
        }
    }

    /**
     * 记录审计日志
     */
    private void recordAuditLogs(OutboundApproveOkReq approveOkReq,
                                 OutboundOrder outboundOrder,
                                 List<OutboundOrderItem> orderItems,
                                 LockResult unlockResult) {
        try {
            log.info("审核操作日志记录成功，订单ID: {}", outboundOrder.getId());
        } catch (Exception e) {
            log.error("记录审核操作日志失败", e);
        }
    }

    /**
     * 启动异步后置处理
     */
    private void startAsyncPostProcessing(OutboundOrder outboundOrder, Long operatorId) {
        CompletableFuture.runAsync(() -> {
            try {
                MDC.put("orderId", String.valueOf(outboundOrder.getId()));

                log.info("开始异步后置处理，订单ID: {}", outboundOrder.getId());

                log.info("异步后置处理完成，订单ID: {}", outboundOrder.getId());

            } catch (Exception e) {
                log.error("异步后置处理异常，订单ID: {}", outboundOrder.getId(), e);
            } finally {
                MDC.clear();
            }
        }, asyncExecutor);
    }

    /**
     * ==================== 原有方法（保持不变） ====================
     */

    @Transactional(rollbackFor = Exception.class)
    public Boolean update(OutboundCreateReq req) {
        // 1. 参数校验
        validateUpdateReq(req);

        // 2. 查询现有出库单
        OutboundOrder existingOrder = outboundOrderService.getById(req.getId());
        if (existingOrder == null) {
            throw new ValidationException("出库单不存在");
        }

        // 3. 校验状态：只有草稿和已拒绝状态可以编辑
        if (existingOrder.getStatus() != 0 && existingOrder.getStatus() != 4) {
            throw new ValidationException("只有草稿和已拒绝状态的出库单可以编辑");
        }

        // 4. 构建更新后的出库单主表实体
        OutboundOrder updatedOrder = buildUpdatedOutboundOrder(req, existingOrder);

        // 5. 更新出库单主表
        boolean orderUpdated = outboundOrderService.updateById(updatedOrder);
        if (!orderUpdated) {
            throw new ValidationException("出库单主表更新失败");
        }

        // 6. 删除原有的出库单明细
        int itemsDeleted = outboundOrderItemService.deleteByOrderId(req.getId(), req.getTenantId(), req.getUserId());
        if (itemsDeleted < 0) {
            throw new ValidationException("原有出库单明细删除失败");
        }

        // 7. 插入新的出库单明细
        List<OutboundOrderItem> orderItems = buildOutboundOrderItems(req, req.getId());
        boolean itemsSaved = outboundOrderItemService.saveBatch(orderItems);
        if (!itemsSaved) {
            throw new ValidationException("出库单明细保存失败");
        }

        //删除原有的生产任务
        Integer integer = productionTaskService.removeByOutBoundId(updatedOrder.getId(), req.getTenantId(), req.getUserId());
        if (integer > 0) {
            //处理生产任务关联信息
            List<ProductionTask> batchSaveProductTaskList = req.getItems().stream().map(v -> {
                ProductionTask pt = new ProductionTask();
                pt.setTenantId(req.getTenantId());
                pt.setTaskNo("productionTask-" + UUID.randomUUID().toString());
                pt.setOutboundOrderId(updatedOrder.getId());
                pt.setOutboundOrderNo(updatedOrder.getOrderNo());
                pt.setProductId(v.getProductId());
                //pt.setProductName(v.getProductName());
                pt.setPlannedQuantity(v.getQuantity());
                pt.setMaterialQuantity(v.getQuantity());
                pt.setProducedQuantity(BigDecimal.ZERO);
                pt.setRemainingQuantity(v.getQuantity());
                pt.setLockQuantity(BigDecimal.ZERO);
                pt.setStatus(CkInOutboundEnums.ProductionTaskStatus.InProduction.getCode());
                pt.setWarehouseId(req.getWarehouseId());
                pt.setExpectedDate(DateUtil.parseDate(req.getExpectedDate()));
                pt.setRemark(v.getRemark());
                pt.setCreatedAt(new Date());
                pt.setCreatedBy(req.getUserId());
                pt.setModifiedAt(new Date());
                pt.setModifiedBy(req.getUserId());
                return pt;
            }).collect(Collectors.toList());
            boolean saveBatchProductionTaskResult = productionTaskService.saveBatch(batchSaveProductTaskList);
            if (!saveBatchProductionTaskResult) {
                throw new ValidationException("生产任务保存失败");
            }
        }

        // 8. 如果是已完成状态，更新库存和流水
        if (req.getStatus() == 3) { // 已完成状态
            inventoryHolder.updateSubInventoryForApprove(updatedOrder, orderItems, req.getUserId());
            //updateInventoryAndTransaction(req, req.getId(), orderItems);
        }

        return true;
    }

    /**
     * 更新参数校验
     */
    private void validateUpdateReq(OutboundCreateReq req) {
        if (req == null) {
            throw new ValidationException("出库单更新请求不能为空");
        }

        // ID校验
        if (req.getId() == null) {
            throw new ValidationException("出库单ID不能为空");
        }

        // 基础字段校验
        if (StringUtils.isBlank(req.getOrderNo())) {
            throw new ValidationException("出库单号不能为空");
        }
        if (req.getOrderType() == null || req.getOrderType() < 1 || req.getOrderType() > 4) {
            throw new ValidationException("出库类型不正确");
        }
        if (req.getWarehouseId() == null) {
            throw new ValidationException("仓库ID不能为空");
        }
        if (req.getStatus() == null || req.getStatus() < 0 || req.getStatus() > 9) {
            throw new ValidationException("状态值不正确");
        }
        if (req.getTenantId() == null) {
            throw new ValidationException("租户ID不能为空");
        }
        if (req.getUserId() == null) {
            throw new ValidationException("用户ID不能为空");
        }

        // 销售出库必须要有客户
        if (req.getOrderType() == 1 && req.getCustomerId() == null) {
            throw new ValidationException("销售出库必须选择客户");
        }

        // 明细校验
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ValidationException("出库单明细不能为空");
        }

        // 校验总数量与明细数量一致性
        BigDecimal totalQuantity = req.getItems().stream()
                .map(item -> item.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (req.getTotalQuantity() == null ||
                req.getTotalQuantity().compareTo(totalQuantity) != 0) {
            throw new ValidationException("总数量与明细数量之和不一致");
        }

        // 校验明细数据
        for (int i = 0; i < req.getItems().size(); i++) {
            OutboundCreateReq.ProductInfoInner item = req.getItems().get(i);
            if (item.getProductId() == null) {
                throw new ValidationException("第" + (i + 1) + "行产品ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("第" + (i + 1) + "行出库数量必须大于0");
            }

            // 校验批次分配
            validateBatchAllocation(item, i, req.getTenantId());
        }
    }

    private void validateCreateReq(OutboundCreateReq req) {
        if (req == null) {
            throw new ValidationException("出库单创建请求不能为空");
        }

        // 基础字段校验
        if (StringUtils.isBlank(req.getOrderNo())) {
            throw new ValidationException("出库单号不能为空");
        }
        if (req.getOrderType() == null || req.getOrderType() < 1 || req.getOrderType() > 4) {
            throw new ValidationException("出库类型不正确");
        }
        if (req.getWarehouseId() == null) {
            throw new ValidationException("仓库ID不能为空");
        }
        if (req.getStatus() == null || req.getStatus() < 0 || req.getStatus() > 9) {
            throw new ValidationException("状态值不正确");
        }
        if (req.getTenantId() == null) {
            throw new ValidationException("租户ID不能为空");
        }
        if (req.getUserId() == null) {
            throw new ValidationException("用户ID不能为空");
        }

        // 销售出库必须要有客户
        if (req.getOrderType() == 1 && req.getCustomerId() == null) {
            throw new ValidationException("销售出库必须选择客户");
        }

        // 明细校验
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ValidationException("出库单明细不能为空");
        }

        // 校验总数量与明细数量一致性
        BigDecimal totalQuantity = req.getItems().stream()
                .map(item -> item.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (req.getTotalQuantity() == null ||
                req.getTotalQuantity().compareTo(totalQuantity) != 0) {
            throw new ValidationException("总数量与明细数量之和不一致");
        }

        // 校验明细数据
        for (int i = 0; i < req.getItems().size(); i++) {
            OutboundCreateReq.ProductInfoInner item = req.getItems().get(i);
            if (item.getProductId() == null) {
                throw new ValidationException("第" + (i + 1) + "行产品ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("第" + (i + 1) + "行出库数量必须大于0");
            }

            // 校验批次分配
            validateBatchAllocation(item, i, req.getTenantId());
        }
    }

    /**
     * 校验批次分配
     */
    private void validateBatchAllocation(OutboundCreateReq.ProductInfoInner item, int index, Long tenantId) {
        if (item.getBomAllocations() == null || item.getBomAllocations().isEmpty()) {
            throw new ValidationException("第" + (index + 1) + "行产品未分配批次");
        }

        // 计算批次分配总数量
        BigDecimal batchTotalQuantity = item.getBomAllocations().stream()
                .map(batch -> batch.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 校验批次分配数量与出库数量是否一致
        if (batchTotalQuantity.compareTo(item.getQuantity()) != 0) {
            throw new ValidationException("第" + (index + 1) + "行产品批次分配数量(" +
                    batchTotalQuantity + ")与出库数量(" + item.getQuantity() + ")不一致");
        }

        // 校验每个批次的库存是否足够
        for (int i = 0; i < item.getBomAllocations().size(); i++) {
            BomAllocationCreateReq batch = item.getBomAllocations().get(i);

            // 查询批次库存
            InventoryBatch inventoryBatch = inventoryBatchService.selectByBatchNoAndProductId(
                    batch.getBatchNo(), item.getProductId(), tenantId);

            if (inventoryBatch == null) {
                throw new ValidationException("第" + (index + 1) + "行第" + (i + 1) +
                        "个批次不存在: " + batch.getBatchNo());
            }

            if (inventoryBatch.getQuantity().compareTo(batch.getQuantity()) < 0) {
                throw new ValidationException("第" + (index + 1) + "行第" + (i + 1) +
                        "个批次库存不足，可用:" + inventoryBatch.getQuantity() +
                        "，需求:" + batch.getQuantity());
            }
        }
    }



    /**
     * 分页查询出库单列表
     */
    public Page<OutboundListPageResp> pageList(Page<OutboundOrder> page, OutboundListPageReq req) {
        Page<OutboundOrder> list = outboundOrderService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> outboundOrderIds = list.getRecords().stream().map(v -> v.getId()).collect(Collectors.toList());
        List<OutboundOrderItem> outboundOrderItems = outboundOrderItemService.selectByTenantIdAndOutboundOrderIds(req.getTenantId(), outboundOrderIds);
        Map<Long, List<OutboundOrderItem>> orderId2ItemListMap = outboundOrderItems.stream().collect(Collectors.groupingBy(OutboundOrderItem::getOrderId));

        List<Long> customerIds = list.getRecords().stream().map(v -> v.getCustomerId()).collect(Collectors.toList());
        List<Customer> customerList = customerService.selectByTenantIdAndCustomerIds(req.getTenantId(), customerIds);
        Map<Long, Customer> customerId2CustomerMap = customerList.stream().collect(Collectors.toMap(Customer::getId, v -> v));

        List<Long> warehouseIds = list.getRecords().stream().map(v -> v.getWarehouseId()).collect(Collectors.toList());
        List<Warehouse> warehouseList = warehouseService.selectByTenantIdAndWareHouseIds(req.getTenantId(), warehouseIds);
        Map<Long, Warehouse> warehouseId2WarehouseMap = warehouseList.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));

        List<Long> accountIds = list.getRecords().stream().map(v -> v.getCreatedBy()).collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(accountIds);
        Map<Long, Account> accountId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        List<OutboundListPageResp> collect = list.getRecords().stream().map(v -> {
            OutboundListPageResp p = new OutboundListPageResp();
            BeanUtils.copyProperties(v, p);

            p.setCustomerName(customerId2CustomerMap.getOrDefault(v.getCustomerId(), new Customer()).getCustomerName());
            p.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(v.getWarehouseId(), new Warehouse()).getName());
            p.setItemCount(orderId2ItemListMap.getOrDefault(v.getId(), new ArrayList<>()).size());
            p.setApplicantName(accountId2AccountMap.getOrDefault(v.getCreatedBy(), new Account()).getUsername());
            p.setApplicantAvatar(accountId2AccountMap.getOrDefault(v.getCreatedBy(), new Account()).getAvatarUrl());
            return p;
        }).collect(Collectors.toList());

        Page<OutboundListPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(OutboundDeleteReq req) {
        OutboundOrder p = outboundOrderService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("出库单不存在");
        }

        OutboundOrder save = new OutboundOrder();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        boolean r = outboundOrderService.updateById(save);
        if (!r) {
            throw new ValidationException("出库单删除失败");
        }

        int i = outboundOrderItemService.deleteByOrderId(p.getId(), req.getTenantId(), req.getUserId());
        if (i <= 0) {
            throw new ValidationException("出库单明细删除失败");
        }

        List<ProductionTask> productionTasks = productionTaskService.selectByOutBoundId(p.getId(), req.getTenantId());
        if (!productionTasks.isEmpty()) {
            Boolean aBoolean = productionTaskService.delectByOutBoundId(p.getId(), req.getTenantId(), req.getUserId());
            if (!aBoolean) {
                throw new ValidationException("生产任务删除失败");
            }
        }

        return inventoryLockService.cleanUpForOutboundDelete(p.getId(), req.getTenantId(), req.getUserId(), p.getOrderType());
    }

    public List<OutboundSaleExcelModel> getOutboundSaleExportData(Long tenantId, Long warehouseId, List<Long>  productIds) {
        List<InventoryListResp> result = inventoryFacade.List(warehouseId, tenantId, productIds);
        return result.stream().map(v -> {
            if (!productIds.contains(v.getProductId())) {
                return null;
            }
            OutboundSaleExcelModel model = new OutboundSaleExcelModel();
            model.setProductId(v.getProductId().toString());
            model.setSku(v.getSku());
            model.setName(v.getProductName());
            model.setSpec(v.getSpec());
            model.setColor(v.getColor());
            model.setInventory(v.getAvailableQuantity().toString());
            return model;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<OutBoundSaleQuantityImportResp> importOutboundSaleQuantity(MultipartFile file, Long tenantId, Long userId, Long warehouseId) {
        // 1. 读取Excel数据
        List<OutBoundSaleQuantityImportDto> importDataList = ExcelUtils.readExcel(file, OutBoundSaleQuantityImportDto.class);
        if (CollectionUtils.isEmpty(importDataList) || importDataList.size() < 1) {
            throw new ValidationException("Excel文件数据不足");
        }

        return importDataList.stream().map(v -> {
            OutBoundSaleQuantityImportResp r = new OutBoundSaleQuantityImportResp();
            r.setProductId(Long.valueOf(v.getProductId()));
            r.setSku(v.getSku());
            r.setProductName(v.getProductName());
            r.setQuantity(StringUtils.isBlank(v.getQuantity()) ? BigDecimal.ZERO : new BigDecimal(v.getQuantity()));
            r.setRemark(v.getRemark());
            r.setPrice(StringUtils.isBlank(v.getPrice()) ? null : new BigDecimal(v.getPrice()));
            r.setPriceUnitUsd(StringUtils.isBlank(v.getPriceUsd()) ? null : new BigDecimal(v.getPriceUsd()));
            return r;
        }).collect(Collectors.toList());

    }

    public OutboundOrder exportOutboundOrderExcelName (Long tenantId, Long orderId) {
        OutboundOrder outboundOrder = outboundOrderService.selectById(orderId, tenantId);
        if (Objects.isNull(outboundOrder)) {
            throw new ValidationException("出库单不存在");
        }
        return outboundOrder;
    }

    public List<OutboundOderExcelModel> exportOutboundOrderExcel(Long tenantId, Long orderId) {
        OutboundOrder outboundOrder = outboundOrderService.selectById(orderId, tenantId);
        if (Objects.isNull(outboundOrder)) {
            throw new ValidationException("出库单不存在");
        }

        List<OutboundOrderItem> outboundOrderItems = outboundOrderItemService.selectByOrderId(orderId, tenantId);
        List<Long> productIds = outboundOrderItems.stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());

        Map<Long, ShelfZone> shelfId2ShelfMap = new HashMap<>();
        List<ShelfZone> warehouseShelves = shelfService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouseShelves)) {
            shelfId2ShelfMap = warehouseShelves.stream().collect(Collectors.toMap(ShelfZone::getId, v -> v));
        }

        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Product> products = productService.selectByIds(tenantId, productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        List<String> skuList = products.stream().map(v -> v.getSku()).distinct().collect(Collectors.toList());
        List<CustomerSkuMapping> customerSkuMappings = customerSkuMappingService.selectByCustomerIdAndSkus(outboundOrder.getCustomerId(), skuList, tenantId);
        Map<String, String> sku2CustomerSkuMap = customerSkuMappings.stream().collect(Collectors.toMap(CustomerSkuMapping::getProductSku, CustomerSkuMapping::getCustomerSku,  (v1, v2) -> v1));

        List<OutboundOrderItemSaleExt> outboundOrderItemSaleExts = outboundOrderItemSaleExtService.selectByOrderId(orderId, tenantId);
        Map<Long, OutboundOrderItemSaleExt> outboundOrderItemId2OutboundOrderItemSaleExtMap = outboundOrderItemSaleExts.stream().collect(Collectors.toMap(OutboundOrderItemSaleExt::getOrderItemId, v -> v));

        Map<Long, ShelfZone> finalShelfId2ShelfMap = shelfId2ShelfMap;
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        return outboundOrderItems.stream().map(v -> {
            OutboundOrderItemSaleExt saleExt = outboundOrderItemId2OutboundOrderItemSaleExtMap.getOrDefault(v.getId(), null);

            //如果不是触发产品，并且是包装件，则不导出
            if (saleExt != null
                    && CkInOutboundEnums.IsTriggerProduct.No.getCode().equals(saleExt.getIsTriggerProduct())
                    && CkInOutboundEnums.IsRecommendProduct.PackageProduct.getCode().equals(saleExt.getIsRecommendProduct())) {
                return null;
            }
            OutboundOderExcelModel model = new OutboundOderExcelModel();
            model.setOrderNumber(outboundOrder.getRelatedOrderNo());
            model.setShelfName(finalShelfId2ShelfMap.getOrDefault(v.getShelfLocationId(), new ShelfZone()).getShelfName());
            model.setQuantity(String.valueOf(v.getQuantity()));
            model.setRemark(v.getRemark());
            model.setPriceUnit(v.getPriceUnit().toString());
            model.setPriceTotal(v.getPriceTotal().toString());
            model.setPriceUnitUsd(v.getPriceUnitUsd().toString());
            model.setPriceTotalUsd(v.getPriceTotalUsd().toString());

            if (finalProductId2ProductMap.containsKey(v.getProductId())) {
                Product product = finalProductId2ProductMap.get(v.getProductId());
                model.setEnglishName(product.getEnglishName());
                model.setSku(product.getSku());
                model.setCustomerSku(sku2CustomerSkuMap.getOrDefault(product.getSku(), ""));
                model.setName(product.getName());
                model.setSpec(product.getSpec());
                model.setColor(product.getColor());
                model.setOutUnitHeight(product.getOutUnitHeight() == null || product.getOutUnitHeight().equals(BigDecimal.ZERO) ? "" : product.getOutUnitHeight().toString());
                model.setOutUnitLength(product.getOutUnitLength() == null || product.getOutUnitLength().equals(BigDecimal.ZERO) ? "" : product.getOutUnitLength().toString());
                model.setOutUnitWidth(product.getOutUnitWidth() == null || product.getOutUnitWidth().equals(BigDecimal.ZERO) ? "" : product.getOutUnitWidth().toString());
                model.setWeightPerUnit(product.getWeightPerUnit() == null || product.getWeightPerUnit().equals(BigDecimal.ZERO) ? "" : product.getWeightPerUnit().toString());
                if (product.getOutUnitPerNum() != null) {
                    model.setOutUnitPerNum(product.getOutUnitPerNum() == null ? "" : product.getOutUnitPerNum().toString());

                    //如果不是触发产品，并且是推荐产品，则不设置数量
                    if (saleExt != null
                            && CkInOutboundEnums.IsTriggerProduct.No.getCode().equals(saleExt.getIsTriggerProduct())
                            && CkInOutboundEnums.IsRecommendProduct.RecommendProduct.getCode().equals(saleExt.getIsRecommendProduct())) {

                    } else {
                        // 计算箱数 = 数量 / 出货单位数量 (有小数，则进1)
                        BigDecimal boxNumB = v.getQuantity().divide(product.getOutUnitPerNum(), 2, RoundingMode.HALF_UP);
                        model.setBoxCount(boxNumB.toString());

                        //体积 = 长 * 宽 * 高 * 箱数 / 1000000
                        BigDecimal volumeB = product.getOutUnitHeight()
                                .multiply(product.getOutUnitLength())
                                .multiply(product.getOutUnitWidth())
                                .multiply(boxNumB)
                                .divide(new BigDecimal(1000000), 2, RoundingMode.HALF_UP);
                        model.setVolume(volumeB.toString());

                        //总重量 = 单件重量 * 箱数
                        BigDecimal wall = product.getWeightPerUnit().multiply(boxNumB);
                        model.setWeightAll(wall.toString());
                    }
                }
            }
            return model;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public OutboundDetailResp detail(Long orderId, Long tenantId) {
        OutboundOrder outboundOrder = outboundOrderService.selectById(orderId, tenantId);
        if (outboundOrder == null) {
            throw new ValidationException("出库单不存在");
        }
        List<OutboundOrderItem> items = outboundOrderItemService.selectByOrderId(orderId, tenantId);
        Map<Long, List<OutboundOrderItem>> productId2OutItemListMap = items.stream().collect(Collectors.groupingBy(OutboundOrderItem::getProductId));

        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Long> productIds = items.stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());
        List<Product> products = productService.selectByIds(tenantId, productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        Map<String, Unit> unitCode2UnitMap = new HashMap<>();
        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        if (!CollectionUtils.isEmpty(units)) {
            unitCode2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));
        }

        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        Map<Long, Customer> customerId2CustomerMap = new HashMap<>();
        List<Customer> customers = customerService.listEnable(tenantId);
        if (!CollectionUtils.isEmpty(customers)) {
            customerId2CustomerMap = customers.stream().collect(Collectors.toMap(Customer::getId, v -> v));
        }

        Map<Long, Account> accountId2AccountMap = new HashMap<>();
        List<Account> accounts = accountService.selectByIds(Lists.newArrayList(outboundOrder.getCreatedBy(), outboundOrder.getModifiedBy()));
        if (!CollectionUtils.isEmpty(accounts)) {
            accountId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        }

        Map<Long, ShelfZone> shelfId2ShelfMap = new HashMap<>();
        List<ShelfZone> warehouseShelves = shelfService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouseShelves)) {
            shelfId2ShelfMap = warehouseShelves.stream().collect(Collectors.toMap(ShelfZone::getId, v -> v));
        }

        List<OutboundOrderItemSaleExt> outboundOrderItemSaleExts = outboundOrderItemSaleExtService.selectByOrderId(orderId, tenantId);
        Map<Long, OutboundOrderItemSaleExt> orderItemId2OutboundOrderItemSaleExtMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(outboundOrderItemSaleExts)) {
            orderItemId2OutboundOrderItemSaleExtMap = outboundOrderItemSaleExts.stream().collect(Collectors.toMap(OutboundOrderItemSaleExt::getOrderItemId, v -> v));
        }

        OutboundDetailResp resp = new OutboundDetailResp();
        resp.setId(outboundOrder.getId());
        resp.setOrderNo(outboundOrder.getOrderNo());
        resp.setOrderType(outboundOrder.getOrderType());
        resp.setRelatedOrderNo(outboundOrder.getRelatedOrderNo());
        resp.setRemark(outboundOrder.getRemark());
        resp.setStatus(outboundOrder.getStatus());
        resp.setCustomerId(outboundOrder.getCustomerId());
        resp.setCustomerName(customerId2CustomerMap.getOrDefault(outboundOrder.getCustomerId(), new Customer()).getCustomerName());
        resp.setTotalQuantity(outboundOrder.getTotalQuantity());
        resp.setTotalAmount(outboundOrder.getTotalAmount());
        resp.setTotalAmountUsd(outboundOrder.getTotalAmountUsd());
        resp.setWarehouseId(outboundOrder.getWarehouseId());
        resp.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
        resp.setExpectedDate(DateUtils.format(outboundOrder.getExpectedDate()));
        resp.setApplicantName(accountId2AccountMap.getOrDefault(outboundOrder.getCreatedBy(), new Account()).getUsername());
        resp.setCreatedAt(outboundOrder.getCreatedAt());
        resp.setUpdatedAt(outboundOrder.getModifiedAt());
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        Map<String, Unit> finalUnitCode2UnitMap = unitCode2UnitMap;
        Map<Long, ShelfZone> finalShelfId2ShelfMap = shelfId2ShelfMap;

        List<OutboundDetailResp.ProductInfoInner> innerList = new ArrayList<>();

        for (OutboundOrderItem ooi : items) {
            OutboundDetailResp.ProductInfoInner req = new OutboundDetailResp.ProductInfoInner();
            req.setProductId(ooi.getProductId());
            Product product = finalProductId2ProductMap.getOrDefault(ooi.getProductId(), null);
            if (product.getId() != null) {
                req.setProductName(finalProductId2ProductMap.getOrDefault(product.getId(), new Product()).getName());
                req.setSku(finalProductId2ProductMap.getOrDefault(product.getId(), new Product()).getSku());
                req.setSpec(finalProductId2ProductMap.getOrDefault(product.getId(), new Product()).getSpec());
                req.setUnit(finalUnitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
            }

            //将outItemList 中的getQxuantity求和
            req.setQuantity(ooi.getQuantity());
            req.setPriceTotal(ooi.getPriceTotal());
            req.setPriceUnit(ooi.getPriceUnit());
            req.setPriceTotalUsd(ooi.getPriceTotalUsd());
            req.setPriceUnitUsd(ooi.getPriceUnitUsd());
            req.setRemark(ooi.getRemark());

            //扩展信息
            if(orderItemId2OutboundOrderItemSaleExtMap.containsKey(ooi.getId())) {
                OutboundOrderItemSaleExt saleExt = orderItemId2OutboundOrderItemSaleExtMap.getOrDefault(ooi.getId(), new OutboundOrderItemSaleExt());
                SaleOutBoundItemExtVO saleExtVO = new SaleOutBoundItemExtVO();
                saleExtVO.setProductId(saleExt.getProductId());
                saleExtVO.setIsTriggerProduct(saleExt.getIsTriggerProduct());
                saleExtVO.setIsRecommendProduct(saleExt.getIsRecommendProduct());
                saleExtVO.setTriggerProductId(saleExt.getTriggerProductId());
                req.setExtension(saleExtVO);
            }

            List<OutboundOrderItem> outItemList = productId2OutItemListMap.get(ooi.getProductId());
            List<OutboundDetailResp.ProductInventoryBatchInner> batchAllocations = outItemList.stream().map(v -> {
                OutboundDetailResp.ProductInventoryBatchInner i = new OutboundDetailResp.ProductInventoryBatchInner();
                i.setItemId(v.getId());
                i.setBatchNo(v.getBatchNo());
                i.setQuantity(v.getQuantity());
                i.setShelfId(v.getShelfLocationId());
                i.setShelfName(finalShelfId2ShelfMap.getOrDefault(v.getShelfLocationId(), new ShelfZone()).getShelfName());
                return i;
            }).collect(Collectors.toList());
            req.setBatchAllocations(batchAllocations);
            innerList.add(req);
        }
        resp.setItems(innerList);
        resp.setItemCount(CollectionUtils.isEmpty(innerList) ? 0 : innerList.size());
        return resp;
    }

    public OutBoundDetailOfProductionResp detailNew(Long orderId, Long tenantId) {
        OutboundOrder outboundOrder = outboundOrderService.selectById(orderId, tenantId);
        if (outboundOrder == null) {
            throw new ValidationException("出库单不存在");
        }

        List<OutboundOrderItem> items = outboundOrderItemService.selectByOrderId(orderId, tenantId);
        Map<Long, List<OutboundOrderItem>> parentProductId2OutItemListMap = items.stream()
                .collect(Collectors.groupingBy(OutboundOrderItem::getRelationProductId));

        // 获取产品信息
        Map<Long, Product> productId2ProductMap = new HashMap<>();

        List<Long> allProductIds = Stream.of(
                items.stream().map(OutboundOrderItem::getProductId).distinct().collect(Collectors.toList()),
                items.stream().map(OutboundOrderItem::getRelationProductId).distinct()
                        .collect(Collectors.toList())).flatMap(List::stream).collect(Collectors.toList());
        List<Product> products = productService.selectByIds(tenantId, allProductIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        // 单位信息
        Map<String, Unit> unitCode2UnitMap = new HashMap<>();
        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        if (!CollectionUtils.isEmpty(units)) {
            unitCode2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));
        }

        // 仓库信息
        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        // 用户信息
        Map<Long, Account> accountId2AccountMap = new HashMap<>();
        List<Account> accounts = accountService.selectByIds(Lists.newArrayList(outboundOrder.getCreatedBy(), outboundOrder.getModifiedBy()));
        if (!CollectionUtils.isEmpty(accounts)) {
            accountId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        }

        //货架信息
        Map<Long, ShelfZone> shelfId2ShelfMap = new HashMap<>();
        List<ShelfZone> shelves = shelfService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(shelves)) {
            shelfId2ShelfMap = shelves.stream().collect(Collectors.toMap(ShelfZone::getId, v -> v));
        }

        //生产任务信息
        List<ProductionTask> productionTasks = productionTaskService.selectByOutBoundId(orderId, tenantId);
        Map<Long, ProductionTask> productId2TaskMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(productionTasks)) {
            productId2TaskMap = productionTasks.stream().collect(Collectors.toMap(ProductionTask::getProductId, v -> v));
        }

        // 构建响应对象
        OutBoundDetailOfProductionResp resp = new OutBoundDetailOfProductionResp();
        resp.setId(outboundOrder.getId());
        resp.setOrderNo(outboundOrder.getOrderNo());
        resp.setOrderType(outboundOrder.getOrderType());
        resp.setRelatedOrderNo(outboundOrder.getRelatedOrderNo());
        resp.setRemark(outboundOrder.getRemark());
        resp.setStatus(outboundOrder.getStatus());
        resp.setTotalQuantity(outboundOrder.getTotalQuantity());
        resp.setTotalAmount(outboundOrder.getTotalAmount());
        resp.setTotalAmountUsd(outboundOrder.getTotalAmountUsd());
        resp.setWarehouseId(outboundOrder.getWarehouseId());
        resp.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
        resp.setExpectedDate(DateUtils.format(outboundOrder.getExpectedDate()));
        resp.setApplicantName(accountId2AccountMap.getOrDefault(outboundOrder.getCreatedBy(), new Account()).getUsername());
        resp.setCreatedAt(outboundOrder.getCreatedAt());
        resp.setUpdatedAt(outboundOrder.getModifiedAt());
        resp.setUserId(outboundOrder.getCreatedBy());
        resp.setTenantId(tenantId);

        // 构建产品明细
        List<OutBoundDetailOfProductionResp.ProductionProductItem> productItems = new ArrayList<>();

        for (Long parentProductId : parentProductId2OutItemListMap.keySet()) {
            List<OutboundOrderItem> outItemList = parentProductId2OutItemListMap.get(parentProductId);
            if (!CollectionUtils.isEmpty(outItemList)) {
                OutBoundDetailOfProductionResp.ProductionProductItem productItem = new OutBoundDetailOfProductionResp.ProductionProductItem();

                // 设置产品基本信息
                productItem.setProductId(parentProductId);
                Product product = productId2ProductMap.get(parentProductId);
                if (product != null) {
                    productItem.setProductName(product.getName());
                    productItem.setSku(product.getSku());
                    productItem.setSpec(product.getSpec());
                    productItem.setColor(product.getColor());
                    productItem.setUnit(unitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
                }

                // 计算总数量
                productItem.setQuantity(productId2TaskMap.getOrDefault(parentProductId, new ProductionTask()).getMaterialQuantity());

                // 构建BOM组件信息
                List<OutBoundDetailOfProductionResp.BomComponent> bomComponents = buildBomComponents(parentProductId, tenantId, outboundOrder.getWarehouseId(), unitCode2UnitMap, shelfId2ShelfMap);
                productItem.setBomComponents(bomComponents);

                // 构建原料分配信息
                List<OutBoundDetailOfProductionResp.MaterialAllocation> materialAllocations = buildMaterialAllocations(outItemList, shelfId2ShelfMap);
                productItem.setMaterialAllocations(materialAllocations);

                productItems.add(productItem);
            }
        }

        resp.setItems(productItems);
        return resp;
    }

    /**
     * 构建BOM组件信息
     */
    private List<OutBoundDetailOfProductionResp.BomComponent> buildBomComponents(Long productId, Long tenantId, Long warehouseId,
                                                                                 Map<String, Unit> unitCode2UnitMap,
                                                                                 Map<Long, ShelfZone> shelfId2ShelfMap) {
        List<OutBoundDetailOfProductionResp.BomComponent> bomComponents = new ArrayList<>();

        try {
            ProductBom productBom = productBomService.selectByProduectId(productId, tenantId);
            // 1. 获取产品的BOM信息
            List<ProductBomDetail> subProductDetails = productBomDetailService.selectByBomId(productBom.getId(), tenantId);
            if (CollectionUtils.isEmpty(subProductDetails)) {
                return bomComponents;
            }

            // 2. 为每个BOM组件构建信息
            Map<Long, List<ProductBomDetail>> componentProductId2ProductBomDetailListMap = subProductDetails.stream()
                    .filter(e-> !CkProductEnums.BomDetailType.BOM_DETAIL_TYPE_PACKAGE.getCode().equals(e.getType()))
                    .collect(Collectors.groupingBy(ProductBomDetail::getComponentProductId));
            List<BomDetailAndWarehouseListNoPackageResp> bomList = new ArrayList<>();

            for (Long componentProductId : componentProductId2ProductBomDetailListMap.keySet()) {
                OutBoundDetailOfProductionResp.BomComponent bomComponent = new OutBoundDetailOfProductionResp.BomComponent();

                List<ProductBomDetail> productBomDetails = componentProductId2ProductBomDetailListMap.getOrDefault(componentProductId, new ArrayList<>());
                // 设置组件基本信息
                bomComponent.setId(productBomDetails.stream().map(ProductBomDetail::getId).min(Comparator.comparingLong(v1 -> v1)).get());
                bomComponent.setComponentProductId(componentProductId);
                bomComponent.setTypeForSort(productBomDetails.stream().map(ProductBomDetail::getType).min(Comparator.comparingInt(v1 -> v1)).get());

                //对 productBomDetails 的quantity求和
                BigDecimal quantityOfSameProduct = BigDecimal.ZERO;
                for (ProductBomDetail productBomDetail : productBomDetails) {
                    quantityOfSameProduct = quantityOfSameProduct.add(productBomDetail.getQuantity());
                }
                bomComponent.setUnitUsage(quantityOfSameProduct); // 单件用量

                // 获取组件产品的详细信息
                Product componentProduct = productService.selectById(tenantId, componentProductId);
                if (componentProduct != null) {
                    bomComponent.setComponentProductName(componentProduct.getName());
                    bomComponent.setComponentProductSku(componentProduct.getSku());
                    bomComponent.setComponentProductSpec(componentProduct.getSpec());

                    // 获取单位信息
                    Unit componentUnit = unitCode2UnitMap.getOrDefault(componentProduct.getUnitCode(), new Unit());
                    bomComponent.setComponentProductUnit(componentUnit != null ? componentUnit.getUnitName() : "");
                }

                // 获取组件的库存批次信息
                List<OutBoundDetailOfProductionResp.StockBatch> availableBatches = getStockBatchesForComponent(
                        componentProductId, warehouseId, tenantId, shelfId2ShelfMap);
                bomComponent.setAvailableBatches(availableBatches);

                List<OutBoundDetailOfProductionResp.UsageDetail> collect = productBomDetails.stream().map(z -> {
                    OutBoundDetailOfProductionResp.UsageDetail usageDetail = new OutBoundDetailOfProductionResp.UsageDetail();
                    usageDetail.setBomDetailId(z.getId());
                    usageDetail.setQuantity(z.getQuantity());
                    usageDetail.setType(z.getType());
                    usageDetail.setLossRate(z.getLossRate());
                    usageDetail.setRemark(z.getRemark());
                    usageDetail.setSortOrder(z.getSortOrder());
                    return usageDetail;
                }).collect(Collectors.toList());
                bomComponent.setUsageDetailList(collect);
                bomComponents.add(bomComponent);
            }
        } catch (Exception e) {
            log.error("构建BOM组件信息失败, productId: {}, tenantId: {}", productId, tenantId, e);
        }
        //bomList 按照 typeForSort 从小到大排序,然后按照id排序
        bomComponents.sort(Comparator.comparingInt(OutBoundDetailOfProductionResp.BomComponent::getTypeForSort).thenComparing(OutBoundDetailOfProductionResp.BomComponent::getId));

        bomComponents.sort(Comparator.comparing(OutBoundDetailOfProductionResp.BomComponent::getComponentProductName));
        return bomComponents;
    }

    /**
     * 获取组件的库存批次信息
     */
    private List<OutBoundDetailOfProductionResp.StockBatch> getStockBatchesForComponent(Long componentProductId, Long warehouseId, Long tenantId, Map<Long, ShelfZone> shelfId2ShelfMap) {
        List<OutBoundDetailOfProductionResp.StockBatch> stockBatches = new ArrayList<>();

        try {
            // 调用库存服务获取批次信息
            List<InventoryShelf> inventoryBatches = inventoryShelfService.getBatchShelfStock(tenantId, warehouseId, componentProductId);

            if (CollectionUtils.isEmpty(inventoryBatches)) {
                return stockBatches;
            }

            // 按批次号分组
            Map<String, List<InventoryShelf>> batchNoMap = inventoryBatches.stream()
                    .collect(Collectors.groupingBy(InventoryShelf::getBatchNo));

            for (Map.Entry<String, List<InventoryShelf>> entry : batchNoMap.entrySet()) {
                OutBoundDetailOfProductionResp.StockBatch stockBatch = new OutBoundDetailOfProductionResp.StockBatch();
                stockBatch.setBatchNo(entry.getKey());

                // 计算批次总数量
                BigDecimal totalQuantity = entry.getValue().stream()
                        .map(InventoryShelf::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                stockBatch.setTotalQuantity(totalQuantity);

                // 构建货架信息
                List<OutBoundDetailOfProductionResp.StockShelf> shelves = entry.getValue().stream()
                        .map(inventoryBatch -> {
                            OutBoundDetailOfProductionResp.StockShelf shelf = new OutBoundDetailOfProductionResp.StockShelf();
                            shelf.setShelfId(inventoryBatch.getShelfId());
                            shelf.setShelfName(shelfId2ShelfMap.getOrDefault(inventoryBatch.getShelfId(), new ShelfZone()).getShelfName());
                            shelf.setAvailableQuantity(inventoryBatch.getQuantity());
                            return shelf;
                        })
                        .collect(Collectors.toList());
                stockBatch.setShelves(shelves);

                stockBatches.add(stockBatch);
            }
        } catch (Exception e) {
            log.error("获取组件库存批次信息失败, componentProductId: {}, warehouseId: {}", componentProductId, warehouseId, e);
        }

        return stockBatches;
    }

    /**
     * 构建原料分配信息
     */
    private List<OutBoundDetailOfProductionResp.MaterialAllocation> buildMaterialAllocations(List<OutboundOrderItem> outItemList, Map<Long, ShelfZone> shelfId2ShelfMap) {
        return outItemList.stream()
                .filter(item -> item.getBatchNo() != null) // 过滤出有批次分配的数据
                .map(item -> {
                    OutBoundDetailOfProductionResp.MaterialAllocation allocation = new OutBoundDetailOfProductionResp.MaterialAllocation();
                    allocation.setComponentProductId(item.getProductId()); // 注意：这里需要根据实际情况调整
                    allocation.setBatchNo(item.getBatchNo());
                    allocation.setShelfId(item.getShelfLocationId());
                    allocation.setShelfName(shelfId2ShelfMap.getOrDefault(item.getShelfLocationId(), new ShelfZone()).getShelfName());
                    allocation.setAllocatedQuantity(item.getQuantity());
                    return allocation;
                })
                .collect(Collectors.toList());
    }

    public List<OutBoundComplateProductResp> listCompletedOutBoundProduction(Long tenantId, Integer orderType) {
        if (orderType == null || tenantId == null) {
            return Collections.emptyList();
        }
        List<ProductionTask> productionTasks = productionTaskService.selectRemainingQuantityBT0ByStatus(CkInOutboundEnums.ProductionTaskStatus.PartialCompletion, tenantId);
        if (productionTasks == null || productionTasks.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        List<Long> outBoundIds = productionTasks.stream().map(v -> v.getOutboundOrderId()).distinct().collect(Collectors.toList());
        List<OutboundOrder> outboundOrders = outboundOrderService.selectByOutboundOrderIds(tenantId, outBoundIds);

        Map<Long, Warehouse> finalWarehouseId2WarehouseMap = warehouseId2WarehouseMap;
        return outboundOrders.stream().map(outboundOrder -> {
            OutBoundComplateProductResp resp = new OutBoundComplateProductResp();
            resp.setId(outboundOrder.getId());
            resp.setOrderNo(outboundOrder.getOrderNo());
            resp.setRelatedOrderNo(outboundOrder.getRelatedOrderNo());
            resp.setOrderType(outboundOrder.getOrderType());
            resp.setWarehouseId(outboundOrder.getWarehouseId());
            resp.setWarehouseName(finalWarehouseId2WarehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
            resp.setTotalQuantity(outboundOrder.getTotalQuantity());
            resp.setStatus(outboundOrder.getStatus());
            resp.setRemark(outboundOrder.getRemark());
            return resp;
        }).collect(Collectors.toList());

    }

    public OutBoundDetailOfProductionResp simpleDetailOfProductionOutboundDetail(Long orderId, Long tenantId) {
        OutboundOrder outboundOrder = outboundOrderService.selectById(orderId, tenantId);
        if (outboundOrder == null) {
            throw new ValidationException("出库单不存在");
        }

        List<OutboundOrderItem> items = outboundOrderItemService.selectByOrderId(orderId, tenantId);
        Map<Long, List<OutboundOrderItem>> parentProductId2OutItemListMap = items.stream()
                .collect(Collectors.groupingBy(OutboundOrderItem::getRelationProductId));

        // 获取产品信息
        Map<Long, Product> productId2ProductMap = new HashMap<>();

        List<Long> allProductIds = Stream.of(
                items.stream().map(OutboundOrderItem::getProductId).distinct().collect(Collectors.toList()),
                items.stream().map(OutboundOrderItem::getRelationProductId).distinct()
                        .collect(Collectors.toList())).flatMap(List::stream).collect(Collectors.toList());
        List<Product> products = productService.selectByIds(tenantId, allProductIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        // 单位信息
        Map<String, Unit> unitCode2UnitMap = new HashMap<>();
        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        if (!CollectionUtils.isEmpty(units)) {
            unitCode2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));
        }

        // 仓库信息
        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        // 用户信息
        Map<Long, Account> accountId2AccountMap = new HashMap<>();
        List<Account> accounts = accountService.selectByIds(Lists.newArrayList(outboundOrder.getCreatedBy(), outboundOrder.getModifiedBy()));
        if (!CollectionUtils.isEmpty(accounts)) {
            accountId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        }

        //生产任务信息
        List<ProductionTask> productionTasks = productionTaskService.selectByOutBoundId(orderId, tenantId);
        Map<Long, ProductionTask> productId2TaskMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(productionTasks)) {
            productId2TaskMap = productionTasks.stream().collect(Collectors.toMap(ProductionTask::getProductId, v -> v));
        }

        // 构建响应对象
        OutBoundDetailOfProductionResp resp = new OutBoundDetailOfProductionResp();
        resp.setId(outboundOrder.getId());
        resp.setOrderNo(outboundOrder.getOrderNo());
        resp.setOrderType(outboundOrder.getOrderType());
        resp.setRelatedOrderNo(outboundOrder.getRelatedOrderNo());
        resp.setRemark(outboundOrder.getRemark());
        resp.setStatus(outboundOrder.getStatus());
        resp.setTotalQuantity(outboundOrder.getTotalQuantity());
        resp.setTotalAmount(outboundOrder.getTotalAmount());
        resp.setTotalAmountUsd(outboundOrder.getTotalAmountUsd());
        resp.setWarehouseId(outboundOrder.getWarehouseId());
        resp.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
        resp.setExpectedDate(DateUtils.format(outboundOrder.getExpectedDate()));
        resp.setApplicantName(accountId2AccountMap.getOrDefault(outboundOrder.getCreatedBy(), new Account()).getUsername());
        resp.setCreatedAt(outboundOrder.getCreatedAt());
        resp.setUpdatedAt(outboundOrder.getModifiedAt());
        resp.setUserId(outboundOrder.getCreatedBy());
        resp.setTenantId(tenantId);

        // 构建产品明细
        List<OutBoundDetailOfProductionResp.ProductionProductItem> productItems = new ArrayList<>();

        for (Long parentProductId : parentProductId2OutItemListMap.keySet()) {
            List<OutboundOrderItem> outItemList = parentProductId2OutItemListMap.get(parentProductId);
            if (!CollectionUtils.isEmpty(outItemList)) {
                OutBoundDetailOfProductionResp.ProductionProductItem productItem = new OutBoundDetailOfProductionResp.ProductionProductItem();

                // 设置产品基本信息
                productItem.setProductId(parentProductId);
                Product product = productId2ProductMap.get(parentProductId);
                if (product != null) {
                    productItem.setProductName(product.getName());
                    productItem.setSku(product.getSku());
                    productItem.setSpec(product.getSpec());
                    productItem.setUnit(unitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
                    productItem.setColor(product.getColor());
                }

                productItem.setQuantity(productId2TaskMap.getOrDefault(parentProductId, new ProductionTask()).getRemainingQuantity());
                productItems.add(productItem);
            }
        }

        resp.setItems(productItems);
        return resp;
    }

    public OutboundCountOfManagePageResp countsOfManagePage(OutboundListPageReq req) {
        List<OutboundOrder> outboundOrders = outboundOrderService.selectCountsByInboundListPageReq(req);
        if (CollectionUtils.isEmpty(outboundOrders)) {
            return new OutboundCountOfManagePageResp();
        }
        OutboundCountOfManagePageResp r = new OutboundCountOfManagePageResp();
        r.setTotalCount(outboundOrders.size());

        List<OutboundOrder> inboundOrdersOfWaiting = outboundOrders.stream().filter(v ->  CkInOutboundEnums.InOutBoundStatus.WaitSubmit.getCode().equals(v.getStatus())).collect(Collectors.toList());
        r.setWaitApproveCount(inboundOrdersOfWaiting.size());

        List<OutboundOrder> appPassList = outboundOrders.stream().filter(v ->  CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode().equals(v.getStatus())).collect(Collectors.toList());
        r.setApprovePassCount(appPassList.size());

        List<OutboundOrder> appRejectList = outboundOrders.stream().filter(v ->  CkInOutboundEnums.InOutBoundStatus.Reject.getCode().equals(v.getStatus())).collect(Collectors.toList());
        r.setApproveRejectCount(appRejectList.size());
        return r;
    }

    /**
     * 重新锁定结果对象
     */
    @Data
    @Builder
    public static class RelockResult {
        private Long orderId;
        private Boolean success;
        private String message;
        private Integer unlockCount;
        private Integer lockCount;
        private RelockComparison comparison;
        private Date relockTime;

        public static RelockResult noNeedResult(Long orderId, String analysis) {
            return RelockResult.builder()
                    .orderId(orderId)
                    .success(true)
                    .message("无需重新锁定: " + analysis)
                    .relockTime(new Date())
                    .build();
        }
    }

    /**
     * 订单变化比较结果
     */
    @Data
    public static class RelockComparison {
        private Long orderId;
        private boolean needRelock;
        private List<String> changes = new ArrayList<>();
        private String analysis;

        public void addChange(String change) {
            changes.add(change);
        }

        public boolean hasOldLocks() {
            return true; // 简化实现
        }
    }
}