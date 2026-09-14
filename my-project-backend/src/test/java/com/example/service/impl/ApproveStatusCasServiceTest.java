package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.example.enums.CkInOutboundEnums;
import com.example.mapper.CkInboundOrderMapper;
import com.example.mapper.CkOutboundOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * FIX-01: CAS UpdateWrapper 条件与返回值（影响行数）契约。
 */
@ExtendWith(MockitoExtension.class)
class ApproveStatusCasServiceTest {

    @Mock
    CkOutboundOrderMapper outboundOrderMapper;
    @Mock
    CkInboundOrderMapper inboundOrderMapper;

    CkOutboundOrderServiceImpl outboundOrderService;
    CkInboundOrderServiceImpl inboundOrderService;

    @BeforeEach
    void setUp() {
        outboundOrderService = new CkOutboundOrderServiceImpl();
        ReflectionTestUtils.setField(outboundOrderService, "baseMapper", outboundOrderMapper);
        inboundOrderService = new CkInboundOrderServiceImpl();
        ReflectionTestUtils.setField(inboundOrderService, "baseMapper", inboundOrderMapper);
    }

    @Test
    void outboundCas_successWhenAffectedRowsOne() {
        when(outboundOrderMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        boolean ok = outboundOrderService.casUpdateStatusForApprove(
                100L, 1L,
                CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode(),
                CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode(),
                9L, new Date(), "[审核通过]");

        assertTrue(ok);
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(outboundOrderMapper).update(isNull(), captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("id"));
        assertTrue(sqlSegment.contains("tenant_id") || sqlSegment.contains("tenant"));
        assertTrue(sqlSegment.contains("status"));
    }

    @Test
    void outboundCas_falseWhenAffectedRowsZero() {
        when(outboundOrderMapper.update(isNull(), any(Wrapper.class))).thenReturn(0);
        assertFalse(outboundOrderService.casUpdateStatusForApprove(
                100L, 1L, 1, 2, 9L, new Date(), null));
    }

    @Test
    void inboundCas_successWhenAffectedRowsOne() {
        when(inboundOrderMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
        assertTrue(inboundOrderService.casUpdateStatusForApprove(
                200L, 1L, 1, 2, 9L, new Date()));
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(inboundOrderMapper).update(isNull(), captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("tenant_id") || sqlSegment.contains("tenant"));
        assertTrue(sqlSegment.contains("status"));
    }

    @Test
    void inboundCas_falseWhenAffectedRowsZero() {
        when(inboundOrderMapper.update(isNull(), any(Wrapper.class))).thenReturn(0);
        assertFalse(inboundOrderService.casUpdateStatusForApprove(
                200L, 1L, 1, 2, 9L, new Date()));
    }
}
