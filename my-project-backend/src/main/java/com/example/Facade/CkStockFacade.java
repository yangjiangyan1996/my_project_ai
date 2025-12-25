package com.example.Facade;

import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 12:04
 */
@Service
public class CkStockFacade {

    // 核心业务流程方法
    public StockTakeVO createFullStockTake(StockTakeCreateDTO dto) {
        // 1. 参数校验
        validateCreateParams(dto);

        // 2. 生成盘点单
        StockTake stockTake = generateStockTake(dto);

        // 3. 根据盘点范围获取库存数据
        List<InventoryItem> inventoryItems =
                inventoryService.getInventoryByScope(dto.getWarehouseId(), dto.getScopeFilter());

        // 4. 生成盘点明细
        List<StockTakeItem> items = generateStockTakeItems(stockTake, inventoryItems);

        // 5. 生成库存快照
        StockSnapshot snapshot = generateStockSnapshot(stockTake, inventoryItems);

        // 6. 保存所有数据（事务保证）
        saveStockTakeData(stockTake, items, snapshot);

        // 7. 记录操作日志
        logOperation(stockTake, "创建全库盘点单");

        return convertToVO(stockTake);
    }

    @Transactional
    public void submitForApproval(Long stockTakeId) {
        // 1. 检查状态
        StockTake stockTake = getById(stockTakeId);
        if (stockTake.getStatus() != StockTakeStatus.DRAFT) {
            throw new BusinessException("只有草稿状态的盘点单可以提交审批");
        }

        // 2. 更新状态
        stockTake.setApprovalStatus(ApprovalStatus.PENDING);
        stockTake.setStatus(StockTakeStatus.WAITING_APPROVAL);
        update(stockTake);

        // 3. 发起审批流程
        approvalService.startStockTakeApproval(stockTake);

        // 4. 发送通知
        notificationService.notifyApprovers(stockTake);
    }

    @Transactional
    public void executeCount(Long stockTakeId, StockTakeExecuteDTO executeDTO) {
        // 1. 验证盘点单状态
        StockTake stockTake = validateExecuteStatus(stockTakeId);

        // 2. 根据盘点策略创建任务
        List<Task> tasks = taskAssignmentService.createCountTasks(stockTake, executeDTO);

        // 3. 更新盘点单状态
        stockTake.setStatus(StockTakeStatus.IN_PROGRESS);
        stockTake.setActualStartTime(new Date());
        update(stockTake);

        // 4. 发布盘点开始事件
        eventPublisher.publishEvent(new StockTakeStartedEvent(stockTake, tasks));
    }
}
