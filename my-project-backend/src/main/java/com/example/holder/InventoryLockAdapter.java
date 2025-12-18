package com.example.holder;

import com.example.entity.cangku.req.OutboundCreateReq;
import com.example.entity.cangku.resp.LockResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 库存锁定适配器
 * 用于在不改变原有业务逻辑的情况下添加库存锁定功能
 */
@Slf4j
@Component
public class InventoryLockAdapter {

    @Autowired
    private CkInventoryLockService inventoryLockService;

    /**
     * 在生产领料出库单创建时执行库存锁定
     * 返回true表示锁定成功，false表示锁定失败（但仍然允许继续）
     */
    public LockResult executeLockForProductionOutbound(OutboundCreateReq req, Long orderId) {
        try {
            LockResult lockResult = inventoryLockService.lockForProductionOutbound(req, orderId);

            if (!lockResult.getSuccess()) {
                log.warn("生产领料出库单库存锁定失败, 订单ID: {}, 原因: {}",
                    orderId, lockResult.getMessage());
            }

            return lockResult;

        } catch (Exception e) {
            log.error("库存锁定执行异常, 订单ID: {}", orderId, e);

            // 返回失败的锁定结果，但允许业务继续
            return LockResult.builder()
                .success(false)
                .message("库存锁定异常: " + e.getMessage())
                .orderId(orderId)
                .build();
        }
    }

    /**
     * 在业务操作失败时回滚库存锁定
     */
    public void rollbackLockIfNeeded(LockResult lockResult, Long orderId, Long tenantId, Long userId) {
        if (lockResult == null || !lockResult.getSuccess() || lockResult.getLockCount() == 0) {
            return;
        }

        try {
            LockResult unlockResult = inventoryLockService.unlockForOutboundCancel(orderId, tenantId, userId);
            if (!unlockResult.getSuccess()) {
                log.error("回滚库存锁定失败, 订单ID: {}", orderId);
                // 这里可以记录到异常表，由定时任务处理
            }
        } catch (Exception e) {
            log.error("回滚库存锁定异常, 订单ID: {}", orderId, e);
        }
    }

    /**
     * 获取锁定状态描述
     */
    public String getLockStatusDescription(LockResult lockResult) {
        if (lockResult == null) {
            return "库存锁定: 未执行";
        }

        if (lockResult.getSuccess()) {
            return String.format("库存锁定: 成功锁定 %d 个批次", lockResult.getLockCount());
        } else {
            return String.format("库存锁定: 部分失败, 成功: %d, 失败: %d",
                lockResult.getLockCount(),
                lockResult.getFailureItems() != null ? lockResult.getFailureItems().size() : 0);
        }
    }
}