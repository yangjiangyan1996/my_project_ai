package com.example.Facade;

import com.example.entity.cangku.dto.OutboundOrder;
import com.example.entity.cangku.dto.OutboundOrderItem;
import com.example.entity.cangku.req.OutboundApproveOkReq;
import com.example.enums.CkInOutboundEnums;
import com.example.holder.InventoryHolder;
import com.example.service.CkInventoryLockService;
import com.example.service.CkOutboundOrderItemService;
import com.example.service.CkOutboundOrderService;
import com.example.service.CkProductionTaskService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FIX-01: 出库审核幂等 — Java 预检 + CAS 在库存 Mutation 之前。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OutboundApproveIdempotencyTest {

    @Mock
    CkOutboundOrderService outboundOrderService;
    @Mock
    CkOutboundOrderItemService outboundOrderItemService;
    @Mock
    CkInventoryLockService stockLockService;
    @Mock
    com.example.holder.CkInventoryLockService inventoryLockService;
    @Mock
    InventoryHolder inventoryHolder;
    @Mock
    CkProductionTaskService productionTaskService;
    @Mock
    Executor asyncExecutor;

    @InjectMocks
    CkOutboundFacade outboundFacade;

    private OutboundApproveOkReq req;
    private OutboundOrder waitAuditOrder;
    private List<OutboundOrderItem> items;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(asyncExecutor).execute(any(Runnable.class));

        req = new OutboundApproveOkReq();
        req.setId(100L);
        req.setTenantId(1L);
        req.setUserId(9L);

        waitAuditOrder = new OutboundOrder();
        waitAuditOrder.setId(100L);
        waitAuditOrder.setTenantId(1L);
        waitAuditOrder.setOrderNo("OUT-100");
        waitAuditOrder.setStatus(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode());
        waitAuditOrder.setOrderType(CkInOutboundEnums.OutBoundType.SaleOutbound.getCode());
        waitAuditOrder.setRemark("");

        OutboundOrderItem item = new OutboundOrderItem();
        item.setId(1L);
        item.setOrderId(100L);
        item.setProductId(10L);
        item.setQuantity(BigDecimal.TEN);
        items = List.of(item);
    }

    @Test
    void firstApprove_success_casThenInventoryOnce() {
        when(outboundOrderService.getById(100L)).thenReturn(waitAuditOrder);
        when(outboundOrderItemService.selectByOrderId(100L, 1L)).thenReturn(items);
        when(outboundOrderService.casUpdateStatusForApprove(
                eq(100L), eq(1L),
                eq(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode()),
                eq(CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode()),
                eq(9L), any(Date.class), anyString())).thenReturn(true);
        when(stockLockService.findBySourceId(1L, 100L)).thenReturn(Collections.emptyList());

        assertTrue(outboundFacade.approveOk(req));

        verify(outboundOrderService).casUpdateStatusForApprove(
                eq(100L), eq(1L),
                eq(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode()),
                eq(CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode()),
                eq(9L), any(Date.class), anyString());
        verify(inventoryHolder, times(1)).updateSubInventoryForApprove(any(), eq(items), eq(9L));
        // CAS before inventory: order of calls
        var inOrder = inOrder(outboundOrderService, inventoryHolder);
        inOrder.verify(outboundOrderService).casUpdateStatusForApprove(
                anyLong(), anyLong(), anyInt(), anyInt(), anyLong(), any(Date.class), anyString());
        inOrder.verify(inventoryHolder).updateSubInventoryForApprove(any(), anyList(), anyLong());
    }

    @Test
    void duplicateApprove_failsOnPrecheck_noInventoryMutation() {
        OutboundOrder approved = new OutboundOrder();
        approved.setId(100L);
        approved.setTenantId(1L);
        approved.setStatus(CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode());

        when(outboundOrderService.getById(100L)).thenReturn(approved);

        ValidationException ex = assertThrows(ValidationException.class, () -> outboundFacade.approveOk(req));
        assertTrue(ex.getMessage().contains("当前状态不允许审核通过"));

        verify(outboundOrderService, never()).casUpdateStatusForApprove(
                anyLong(), anyLong(), anyInt(), anyInt(), anyLong(), any(Date.class), anyString());
        verify(inventoryHolder, never()).updateSubInventoryForApprove(any(), anyList(), anyLong());
        verify(stockLockService, never()).findBySourceId(anyLong(), anyLong());
    }

    @Test
    void illegalCancelStatus_failsBeforeInventory() {
        OutboundOrder cancelled = new OutboundOrder();
        cancelled.setId(100L);
        cancelled.setTenantId(1L);
        cancelled.setStatus(CkInOutboundEnums.InOutBoundStatus.Cancel.getCode());

        when(outboundOrderService.getById(100L)).thenReturn(cancelled);

        assertThrows(ValidationException.class, () -> outboundFacade.approveOk(req));
        verify(inventoryHolder, never()).updateSubInventoryForApprove(any(), anyList(), anyLong());
        verify(outboundOrderService, never()).casUpdateStatusForApprove(
                anyLong(), anyLong(), anyInt(), anyInt(), anyLong(), any(Date.class), anyString());
    }

    @Test
    void casConflict_failsBeforeInventory() {
        when(outboundOrderService.getById(100L)).thenReturn(waitAuditOrder);
        when(outboundOrderItemService.selectByOrderId(100L, 1L)).thenReturn(items);
        when(outboundOrderService.casUpdateStatusForApprove(
                anyLong(), anyLong(), anyInt(), anyInt(), anyLong(), any(Date.class), anyString()))
                .thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class, () -> outboundFacade.approveOk(req));
        assertEquals("订单状态已变化，请刷新后重试", ex.getMessage());

        verify(inventoryHolder, never()).updateSubInventoryForApprove(any(), anyList(), anyLong());
        verify(stockLockService, never()).findBySourceId(anyLong(), anyLong());
    }
}
