package com.example.Facade;

import com.example.entity.cangku.dto.InboundOrder;
import com.example.entity.cangku.dto.InboundOrderItem;
import com.example.entity.cangku.req.InboundApproveOkReq;
import com.example.enums.CkInOutboundEnums;
import com.example.holder.InventoryHolder;
import com.example.service.CkInboundOrderItemService;
import com.example.service.CkInboundOrderService;
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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FIX-01: 入库审核幂等 — Java 预检 + CAS 在库存 Mutation 之前。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InboundApproveIdempotencyTest {

    @Mock
    CkInboundOrderService inboundOrderService;
    @Mock
    CkInboundOrderItemService inboundOrderItemService;
    @Mock
    InventoryHolder inventoryHolder;
    @Mock
    CkProductionTaskService productionTaskService;

    @InjectMocks
    CkInboundFacade inboundFacade;

    private InboundApproveOkReq req;
    private InboundOrder waitAuditOrder;
    private List<InboundOrderItem> items;

    @BeforeEach
    void setUp() {
        req = new InboundApproveOkReq();
        req.setOrderId(200L);
        req.setTenantId(1L);
        req.setUserId(9L);

        waitAuditOrder = new InboundOrder();
        waitAuditOrder.setId(200L);
        waitAuditOrder.setTenantId(1L);
        waitAuditOrder.setOrderNo("IN-200");
        waitAuditOrder.setStatus(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode());

        InboundOrderItem item = new InboundOrderItem();
        item.setId(1L);
        item.setOrderId(200L);
        item.setProductId(10L);
        item.setActualQuantity(BigDecimal.ONE);
        items = List.of(item);
    }

    @Test
    void firstApprove_success_casThenInventoryOnce() {
        when(inboundOrderService.selectById(200L, 1L)).thenReturn(waitAuditOrder);
        when(inboundOrderItemService.selectByInboundOrderId(1L, 200L)).thenReturn(items);
        when(inboundOrderService.casUpdateStatusForApprove(
                eq(200L), eq(1L),
                eq(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode()),
                eq(CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode()),
                eq(9L), any(Date.class))).thenReturn(true);

        assertTrue(inboundFacade.approveOk(req));

        verify(inventoryHolder, times(1)).updateAddInventoryForApprove(any(), eq(items), eq(9L));
        var inOrder = inOrder(inboundOrderService, inventoryHolder);
        inOrder.verify(inboundOrderService).casUpdateStatusForApprove(
                anyLong(), anyLong(), anyInt(), anyInt(), anyLong(), any(Date.class));
        inOrder.verify(inventoryHolder).updateAddInventoryForApprove(any(), anyList(), anyLong());
    }

    @Test
    void duplicateApprove_failsOnPrecheck_noInventoryMutation() {
        InboundOrder approved = new InboundOrder();
        approved.setId(200L);
        approved.setTenantId(1L);
        approved.setStatus(CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode());
        when(inboundOrderService.selectById(200L, 1L)).thenReturn(approved);

        ValidationException ex = assertThrows(ValidationException.class, () -> inboundFacade.approveOk(req));
        assertTrue(ex.getMessage().contains("当前状态不允许审核通过"));

        verify(inboundOrderService, never()).casUpdateStatusForApprove(
                anyLong(), anyLong(), anyInt(), anyInt(), anyLong(), any(Date.class));
        verify(inventoryHolder, never()).updateAddInventoryForApprove(any(), anyList(), anyLong());
    }

    @Test
    void illegalCompleteStatus_failsBeforeInventory() {
        InboundOrder complete = new InboundOrder();
        complete.setId(200L);
        complete.setTenantId(1L);
        complete.setStatus(CkInOutboundEnums.InOutBoundStatus.InOutboundComplete.getCode());
        when(inboundOrderService.selectById(200L, 1L)).thenReturn(complete);

        assertThrows(ValidationException.class, () -> inboundFacade.approveOk(req));
        verify(inventoryHolder, never()).updateAddInventoryForApprove(any(), anyList(), anyLong());
    }

    @Test
    void casConflict_failsBeforeInventory() {
        when(inboundOrderService.selectById(200L, 1L)).thenReturn(waitAuditOrder);
        when(inboundOrderItemService.selectByInboundOrderId(1L, 200L)).thenReturn(items);
        when(inboundOrderService.casUpdateStatusForApprove(
                anyLong(), anyLong(), anyInt(), anyInt(), anyLong(), any(Date.class)))
                .thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class, () -> inboundFacade.approveOk(req));
        assertEquals("订单状态已变化，请刷新后重试", ex.getMessage());
        verify(inventoryHolder, never()).updateAddInventoryForApprove(any(), anyList(), anyLong());
    }
}
