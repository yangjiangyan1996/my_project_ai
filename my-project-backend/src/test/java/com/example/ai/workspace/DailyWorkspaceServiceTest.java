package com.example.ai.workspace;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.llm.FakeLlmClient;
import com.example.ai.llm.LlmClient;
import com.example.ai.prompt.SystemPromptFactory;
import com.example.Facade.CkInboundFacade;
import com.example.Facade.CkInventoryFacade;
import com.example.Facade.CkOutboundFacade;
import com.example.Facade.CkStockFacade;
import com.example.entity.cangku.req.InboundListPageReq;
import com.example.entity.cangku.req.InventoryAlertReq;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.entity.cangku.req.StockListPageReq;
import com.example.entity.cangku.resp.InboundListPageResp;
import com.example.entity.cangku.resp.InventoryAlertResp;
import com.example.entity.cangku.resp.InventoryCountsOfIndexPageResp;
import com.example.entity.cangku.resp.OutboundListPageResp;
import com.example.entity.cangku.resp.StockListPageResp;
import com.example.enums.CkInOutboundEnums;
import com.example.enums.CkStockTakeEnums;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class DailyWorkspaceServiceTest {

    private CkInventoryFacade inventoryFacade;
    private CkInboundFacade inboundFacade;
    private CkOutboundFacade outboundFacade;
    private CkStockFacade stockFacade;
    private DailyWorkspaceQueryService queryService;
    private DailyWorkspaceService service;
    private AiExecutionContext ctx;

    @BeforeEach
    void setUp() {
        inventoryFacade = mock(CkInventoryFacade.class);
        inboundFacade = mock(CkInboundFacade.class);
        outboundFacade = mock(CkOutboundFacade.class);
        stockFacade = mock(CkStockFacade.class);

        queryService = new DailyWorkspaceQueryService();
        ReflectionTestUtils.setField(queryService, "inventoryFacade", inventoryFacade);
        ReflectionTestUtils.setField(queryService, "inboundFacade", inboundFacade);
        ReflectionTestUtils.setField(queryService, "outboundFacade", outboundFacade);
        ReflectionTestUtils.setField(queryService, "stockFacade", stockFacade);

        service = new DailyWorkspaceService();
        ReflectionTestUtils.setField(service, "queryService", queryService);
        ReflectionTestUtils.setField(service, "systemPromptFactory", new SystemPromptFactory());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());

        ctx = AiExecutionContext.builder()
                .userId(1L).tenantId(88L).username("u")
                .conversationId("c").requestId("r")
                .permissions(Set.of())
                .build();

        InventoryCountsOfIndexPageResp index = new InventoryCountsOfIndexPageResp();
        index.setTodayInbound(2L);
        index.setTodayOutbound(3L);
        index.setLowStock(4L);
        index.setTodoInBoundApproval(0L);
        index.setTodoOutBoundApproval(0L);
        when(inventoryFacade.countsOfIndexPage(88L)).thenReturn(index);

        when(inboundFacade.pageList(any(), any(InboundListPageReq.class))).thenAnswer(inv -> {
            InboundListPageReq req = inv.getArgument(1);
            assertEquals(88L, req.getTenantId());
            assertEquals(Integer.valueOf(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode()), req.getStatus());
            Page<InboundListPageResp> page = new Page<>(1, req.getSize());
            page.setTotal(8);
            InboundListPageResp sample = new InboundListPageResp();
            sample.setId(11L);
            sample.setOrderNo("IN-1");
            sample.setWarehouseName("杭州仓");
            page.setRecords(List.of(sample));
            return page;
        });
        when(outboundFacade.pageList(any(), any(OutboundListPageReq.class))).thenAnswer(inv -> {
            OutboundListPageReq req = inv.getArgument(1);
            assertEquals(88L, req.getTenantId());
            Page<OutboundListPageResp> page = new Page<>(1, req.getSize());
            page.setTotal(6);
            OutboundListPageResp sample = new OutboundListPageResp();
            sample.setId(21L);
            sample.setOrderNo("OUT-1");
            sample.setWarehouseName("杭州仓");
            page.setRecords(List.of(sample));
            return page;
        });
        when(stockFacade.pageList(any(), any(StockListPageReq.class))).thenAnswer(inv -> {
            Page<StockListPageResp> page = new Page<>(1, 50);
            StockListPageResp pending = new StockListPageResp();
            pending.setId(31L);
            pending.setStockTakeNo("ST-1");
            pending.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW.getCode());
            pending.setWarehouseName("杭州仓");
            StockListPageResp other = new StockListPageResp();
            other.setId(32L);
            other.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.APPROVED.getCode());
            page.setRecords(List.of(pending, other));
            page.setTotal(2);
            return page;
        });
        InventoryAlertResp alert = new InventoryAlertResp();
        alert.setProductId(99L);
        alert.setSku("A001");
        alert.setAlertLevel("urgent");
        alert.setCurrentStock(new BigDecimal("1"));
        alert.setWarehouseName("杭州仓");
        when(inventoryFacade.getInventoryAlerts(any(InventoryAlertReq.class))).thenReturn(List.of(alert));
    }

    @Test
    void aggregatesPendingAndRisk_fromFacades() {
        ReflectionTestUtils.setField(service, "llmClient", new FakeLlmClient("今天有 8 张待入库需要处理。"));
        AiDailyWorkspaceResponse resp = service.load(ctx);
        assertNotNull(resp.getData());
        assertEquals(8L, resp.getData().getInbound().getPendingCount());
        assertEquals(6L, resp.getData().getOutbound().getPendingCount());
        assertEquals(1L, resp.getData().getStocktake().getPendingCount());
        assertEquals(4L, resp.getData().getInventory().getRiskCount());
        assertEquals(2L, resp.getData().getInbound().getTodayCount());
        assertTrue(resp.isAiSummaryAvailable());
        assertTrue(resp.getAiSummary().contains("8"));
        assertFalse(resp.getData().getPriorityItems().isEmpty());
        verify(inventoryFacade).countsOfIndexPage(88L);
        verify(inboundFacade, atLeastOnce()).pageList(any(), any());
    }

    @Test
    void llmFailure_stillReturnsStructuredData() {
        AtomicBoolean called = new AtomicBoolean();
        LlmClient failing = new LlmClient() {
            @Override
            public com.example.ai.llm.LlmChatResult chat(com.example.ai.llm.LlmChatRequest request) {
                called.set(true);
                throw new RuntimeException("DeepSeek down");
            }
            @Override public String providerId() { return "fake"; }
            @Override public String defaultModel() { return "fake"; }
        };
        ReflectionTestUtils.setField(service, "llmClient", failing);
        AiDailyWorkspaceResponse resp = service.load(ctx);
        assertTrue(called.get());
        assertFalse(resp.isAiSummaryAvailable());
        assertEquals("AI 摘要暂时不可用", resp.getAiSummary());
        assertEquals(8L, resp.getData().getInbound().getPendingCount());
        assertEquals(6L, resp.getData().getOutbound().getPendingCount());
    }

    @Test
    void noSideEffects_onlyQueryFacades() {
        ReflectionTestUtils.setField(service, "llmClient", new FakeLlmClient("摘要"));
        service.load(ctx);
        verify(inventoryFacade, atLeastOnce()).countsOfIndexPage(anyLong());
        verify(inventoryFacade, atLeastOnce()).getInventoryAlerts(any());
        verify(inboundFacade, atLeastOnce()).pageList(any(), any());
        verify(outboundFacade, atLeastOnce()).pageList(any(), any());
        verify(stockFacade, atLeastOnce()).pageList(any(), any());
        verifyNoMoreInteractions(inventoryFacade, inboundFacade, outboundFacade, stockFacade);
    }
}
