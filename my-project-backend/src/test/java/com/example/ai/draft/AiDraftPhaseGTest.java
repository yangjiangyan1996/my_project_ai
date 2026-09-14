package com.example.ai.draft;

import com.example.ai.audit.LoggingAiAuditRecorder;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.permission.AiPermissionChecker;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.draft.PrepareInboundDraftTool;
import com.example.ai.tool.draft.PrepareOutboundDraftTool;
import com.example.ai.tool.draft.PrepareStocktakeDraftTool;
import com.example.Facade.CKProductFacade;
import com.example.Facade.CkCustomerFacade;
import com.example.Facade.CkInboundFacade;
import com.example.Facade.CkInventoryFacade;
import com.example.Facade.CkOutboundFacade;
import com.example.Facade.CkShelfFacade;
import com.example.Facade.CkStockFacade;
import com.example.Facade.CkSupplierFacade;
import com.example.Facade.CkWarehouseFacade;
import com.example.entity.cangku.req.InboundCreateReq;
import com.example.entity.cangku.req.OutboundCreateSaleProductReq;
import com.example.entity.cangku.req.StockTakeCreateReq;
import com.example.entity.cangku.resp.CustomerPageListResp;
import com.example.entity.cangku.resp.InventoryBatchResp;
import com.example.entity.cangku.resp.ProductSimpleListResp;
import com.example.entity.cangku.resp.ShelfEnalbedListResp;
import com.example.entity.cangku.resp.SupplierPageListResp;
import com.example.entity.cangku.resp.WareHouseResp;
import com.example.service.CkStockTakeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Phase G — Draft Assistant unit tests (mocked Facades, InMemory store).
 */
class AiDraftPhaseGTest {

    private InMemoryAiDraftStore store;
    private AiDraftService draftService;
    private AiDraftOrderCreator orderCreator;
    private DraftEntityResolver resolver;
    private PrepareOutboundDraftTool outboundTool;
    private PrepareInboundDraftTool inboundTool;
    private PrepareStocktakeDraftTool stocktakeTool;

    private CkCustomerFacade customerFacade;
    private CkSupplierFacade supplierFacade;
    private CkWarehouseFacade warehouseFacade;
    private CKProductFacade productFacade;
    private CkInventoryFacade inventoryFacade;
    private CkShelfFacade shelfFacade;
    private CkOutboundFacade outboundFacade;
    private CkInboundFacade inboundFacade;
    private CkStockFacade stockFacade;

    private AiExecutionContext ctx;
    private AiExecutionContext otherUser;
    private AiExecutionContext otherTenant;

    @BeforeEach
    void setUp() {
        store = new InMemoryAiDraftStore();
        customerFacade = mock(CkCustomerFacade.class);
        supplierFacade = mock(CkSupplierFacade.class);
        warehouseFacade = mock(CkWarehouseFacade.class);
        productFacade = mock(CKProductFacade.class);
        inventoryFacade = mock(CkInventoryFacade.class);
        shelfFacade = mock(CkShelfFacade.class);
        outboundFacade = mock(CkOutboundFacade.class);
        inboundFacade = mock(CkInboundFacade.class);
        stockFacade = mock(CkStockFacade.class);
        CkStockTakeService stockTakeService = mock(CkStockTakeService.class);

        resolver = new DraftEntityResolver();
        ReflectionTestUtils.setField(resolver, "customerFacade", customerFacade);
        ReflectionTestUtils.setField(resolver, "supplierFacade", supplierFacade);
        ReflectionTestUtils.setField(resolver, "warehouseFacade", warehouseFacade);
        ReflectionTestUtils.setField(resolver, "productFacade", productFacade);
        ReflectionTestUtils.setField(resolver, "inventoryFacade", inventoryFacade);
        ReflectionTestUtils.setField(resolver, "shelfFacade", shelfFacade);

        orderCreator = new AiDraftOrderCreator();
        ReflectionTestUtils.setField(orderCreator, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(orderCreator, "inboundFacade", inboundFacade);
        ReflectionTestUtils.setField(orderCreator, "outboundFacade", outboundFacade);
        ReflectionTestUtils.setField(orderCreator, "stockFacade", stockFacade);
        ReflectionTestUtils.setField(orderCreator, "stockTakeService", stockTakeService);

        draftService = new AiDraftService();
        ReflectionTestUtils.setField(draftService, "draftStore", store);
        ReflectionTestUtils.setField(draftService, "orderCreator", orderCreator);
        ReflectionTestUtils.setField(draftService, "permissionChecker", new AiPermissionChecker());
        ReflectionTestUtils.setField(draftService, "auditRecorder", new LoggingAiAuditRecorder());
        ReflectionTestUtils.setField(draftService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(draftService, "ttlMinutes", 60L);

        outboundTool = new PrepareOutboundDraftTool();
        ReflectionTestUtils.setField(outboundTool, "resolver", resolver);
        ReflectionTestUtils.setField(outboundTool, "draftService", draftService);

        inboundTool = new PrepareInboundDraftTool();
        ReflectionTestUtils.setField(inboundTool, "resolver", resolver);
        ReflectionTestUtils.setField(inboundTool, "draftService", draftService);

        stocktakeTool = new PrepareStocktakeDraftTool();
        ReflectionTestUtils.setField(stocktakeTool, "resolver", resolver);
        ReflectionTestUtils.setField(stocktakeTool, "draftService", draftService);

        ctx = AiExecutionContext.builder()
                .userId(1L).tenantId(88L).username("u1").conversationId("c1")
                .permissions(Set.of())
                .build();
        otherUser = AiExecutionContext.builder()
                .userId(2L).tenantId(88L).username("u2").conversationId("c2")
                .permissions(Set.of())
                .build();
        otherTenant = AiExecutionContext.builder()
                .userId(1L).tenantId(99L).username("u1").conversationId("c3")
                .permissions(Set.of())
                .build();

        stubHappyPathEntities();
    }

    private void stubHappyPathEntities() {
        CustomerPageListResp cust = new CustomerPageListResp();
        cust.setId(10L);
        cust.setCustomerName("杭州ABC");
        cust.setCustomerCode("C001");
        when(customerFacade.pageList(any(), any())).thenAnswer(inv -> {
            var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<CustomerPageListResp>(1, 20);
            page.setRecords(List.of(cust));
            page.setTotal(1);
            return page;
        });
        com.example.entity.cangku.resp.CustomerEnabledListResp custEn =
                new com.example.entity.cangku.resp.CustomerEnabledListResp();
        custEn.setId(10L);
        custEn.setCustomerName("杭州ABC");
        custEn.setCustomerCode("C001");
        when(customerFacade.listEnable(any())).thenReturn(List.of(custEn));

        WareHouseResp wh = new WareHouseResp();
        wh.setId(20L);
        wh.setName("杭州一号仓");
        wh.setCode("WH01");
        when(warehouseFacade.listOfWareHouse(any(), any())).thenAnswer(inv -> {
            var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<WareHouseResp>(1, 20);
            page.setRecords(List.of(wh));
            page.setTotal(1);
            return page;
        });
        when(warehouseFacade.listEnable(any())).thenReturn(List.of(wh));

        ProductSimpleListResp p1 = new ProductSimpleListResp();
        p1.setId(100L);
        p1.setSku("A001");
        p1.setName("商品A");
        ProductSimpleListResp p2 = new ProductSimpleListResp();
        p2.setId(101L);
        p2.setSku("B002");
        p2.setName("商品B");
        when(productFacade.productSimpleList(any(), eq("A001"))).thenReturn(List.of(p1));
        when(productFacade.productSimpleList(any(), eq("B002"))).thenReturn(List.of(p2));
        when(productFacade.productSimpleList(any(), isNull())).thenReturn(List.of());
        when(productFacade.pageList(any(), any())).thenAnswer(inv -> {
            var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.example.entity.cangku.resp.ProductPageListResp>(1, 20);
            Object req = inv.getArgument(1);
            String sku = null;
            try {
                sku = (String) req.getClass().getMethod("getSku").invoke(req);
            } catch (Exception ignored) {
            }
            com.example.entity.cangku.resp.ProductPageListResp pp1 = new com.example.entity.cangku.resp.ProductPageListResp();
            pp1.setId(100L);
            pp1.setSku("A001");
            pp1.setName("商品A");
            com.example.entity.cangku.resp.ProductPageListResp pp2 = new com.example.entity.cangku.resp.ProductPageListResp();
            pp2.setId(101L);
            pp2.setSku("B002");
            pp2.setName("商品B");
            if (sku != null && !sku.isBlank()) {
                if ("A001".equalsIgnoreCase(sku)) {
                    page.setRecords(List.of(pp1));
                    page.setTotal(1);
                } else if ("B002".equalsIgnoreCase(sku)) {
                    page.setRecords(List.of(pp2));
                    page.setTotal(1);
                } else {
                    page.setRecords(List.of());
                    page.setTotal(0);
                }
            } else {
                page.setRecords(List.of(pp1, pp2));
                page.setTotal(2);
            }
            return page;
        });

        InventoryBatchResp.ShelfInfo shelf = new InventoryBatchResp.ShelfInfo();
        shelf.setShelfId(5L);
        shelf.setShelfName("A-1");
        shelf.setQuantity(new BigDecimal("1000"));
        InventoryBatchResp batch = new InventoryBatchResp();
        batch.setBatchNo("B1");
        batch.setQuantity(new BigDecimal("1000"));
        batch.setShelfList(List.of(shelf));
        when(inventoryFacade.batches(anyLong(), anyLong(), anyLong())).thenReturn(List.of(batch));

        SupplierPageListResp sup = new SupplierPageListResp();
        sup.setId(30L);
        sup.setSupplierName("供应商X");
        sup.setSupplierCode("S001");
        when(supplierFacade.pageList(any(), any())).thenAnswer(inv -> {
            var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<SupplierPageListResp>(1, 20);
            page.setRecords(List.of(sup));
            page.setTotal(1);
            return page;
        });
        when(supplierFacade.listEnable(any())).thenReturn(List.of(sup));

        ShelfEnalbedListResp shelfEnable = new ShelfEnalbedListResp();
        shelfEnable.setId(5L);
        shelfEnable.setShelfName("A-1");
        when(shelfFacade.listEnable(anyLong(), anyLong())).thenReturn(List.of(shelfEnable));
    }

    @Test
    void outbound_fullNaturalLanguage_toDraft() {
        ToolResult r = outboundTool.execute(Map.of(
                "customerKeyword", "杭州ABC",
                "warehouseKeyword", "杭州一号仓",
                "items", List.of(
                        Map.of("sku", "A001", "quantity", 100),
                        Map.of("sku", "B002", "quantity", 50)
                ),
                "parsedInput", "帮我给杭州ABC创建出库单"
        ), ctx);
        assertTrue(r.isSuccess(), () -> "msg=" + r.getMessage() + " data=" + r.getData());
        assertEquals("DRAFT", r.getData().get("responseType"), () -> String.valueOf(r.getData()));
        assertNotNull(r.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        Map<String, Object> draft = (Map<String, Object>) r.getData().get("draft");
        assertEquals("OPEN", draft.get("status"));
        assertNotNull(draft.get("confirmToken"));
    }

    @Test
    void outbound_missingCustomer_needClarification() {
        ToolResult r = outboundTool.execute(Map.of(
                "warehouseKeyword", "杭州一号仓",
                "items", List.of(Map.of("sku", "A001", "quantity", 100))
        ), ctx);
        assertTrue(r.isSuccess());
        assertTrue(Boolean.TRUE.equals(r.getData().get("needClarification")));
        assertTrue(r.getMessage().contains("客户"));
    }

    @Test
    void outbound_missingWarehouse_needClarification() {
        when(warehouseFacade.listEnable(any())).thenReturn(List.of());
        ToolResult r = outboundTool.execute(Map.of(
                "customerKeyword", "杭州ABC",
                "items", List.of(Map.of("sku", "A001", "quantity", 100))
        ), ctx);
        assertTrue(r.isSuccess());
        assertTrue(Boolean.TRUE.equals(r.getData().get("needClarification")));
        assertTrue(r.getMessage().contains("仓库"));
    }

    @Test
    void outbound_multiCustomer_clarification() {
        CustomerPageListResp c1 = new CustomerPageListResp();
        c1.setId(10L);
        c1.setCustomerName("杭州ABC");
        CustomerPageListResp c2 = new CustomerPageListResp();
        c2.setId(11L);
        c2.setCustomerName("杭州ABC分公司");
        when(customerFacade.pageList(any(), any())).thenAnswer(inv -> {
            var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<CustomerPageListResp>(1, 20);
            page.setRecords(List.of(c1, c2));
            page.setTotal(2);
            return page;
        });
        ToolResult r = outboundTool.execute(Map.of(
                "customerKeyword", "杭州ABC",
                "warehouseKeyword", "杭州一号仓",
                "items", List.of(Map.of("sku", "A001", "quantity", 1))
        ), ctx);
        assertTrue(Boolean.TRUE.equals(r.getData().get("ambiguous"))
                || Boolean.TRUE.equals(r.getData().get("needClarification")));
        assertTrue(((List<?>) r.getData().get("items")).size() > 1);
    }

    @Test
    void outbound_multiWarehouse_clarification() {
        WareHouseResp w1 = new WareHouseResp();
        w1.setId(20L);
        w1.setName("杭州一号仓");
        WareHouseResp w2 = new WareHouseResp();
        w2.setId(21L);
        w2.setName("杭州二号仓");
        when(warehouseFacade.listOfWareHouse(any(), any())).thenAnswer(inv -> {
            var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<WareHouseResp>(1, 20);
            page.setRecords(List.of(w1, w2));
            page.setTotal(2);
            return page;
        });
        ToolResult r = outboundTool.execute(Map.of(
                "customerKeyword", "杭州ABC",
                "warehouseKeyword", "杭州仓",
                "items", List.of(Map.of("sku", "A001", "quantity", 1))
        ), ctx);
        assertTrue(((List<?>) r.getData().get("items")).size() > 1);
    }

    @Test
    void outbound_confirm_once_and_duplicate_blocked() {
        ToolResult prepared = outboundTool.execute(Map.of(
                "customerId", 10L,
                "warehouseId", 20L,
                "items", List.of(Map.of("productId", 100L, "sku", "A001", "quantity", 10))
        ), ctx);
        String draftId = String.valueOf(prepared.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        Map<String, Object> card = (Map<String, Object>) prepared.getData().get("draft");
        String token = String.valueOf(card.get("confirmToken"));

        when(outboundFacade.createProductionSaleOutBound(any(OutboundCreateSaleProductReq.class))).thenReturn(true);

        AiDraftConfirmResult ok = draftService.confirm(ctx, draftId, token);
        assertTrue(ok.isSuccess());
        assertEquals(DraftStatus.CONFIRMED, ok.getDraftStatus());
        verify(outboundFacade, times(1)).createProductionSaleOutBound(any());

        AiDraftConfirmResult dup = draftService.confirm(ctx, draftId, token);
        assertFalse(dup.isSuccess());
        assertEquals("ALREADY_CONFIRMED", dup.getErrorCode());
        verify(outboundFacade, times(1)).createProductionSaleOutBound(any());
    }

    @Test
    void outbound_cancelled_cannot_confirm() {
        ToolResult prepared = outboundTool.execute(Map.of(
                "customerId", 10L,
                "warehouseId", 20L,
                "items", List.of(Map.of("productId", 100L, "quantity", 1))
        ), ctx);
        String draftId = String.valueOf(prepared.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        String token = String.valueOf(((Map<?, ?>) prepared.getData().get("draft")).get("confirmToken"));
        assertTrue(draftService.cancel(ctx, draftId).isSuccess());
        AiDraftConfirmResult r = draftService.confirm(ctx, draftId, token);
        assertEquals("DRAFT_CANCELLED", r.getErrorCode());
        verify(outboundFacade, never()).createProductionSaleOutBound(any());
    }

    @Test
    void outbound_expired_cannot_confirm() {
        ToolResult prepared = outboundTool.execute(Map.of(
                "customerId", 10L,
                "warehouseId", 20L,
                "items", List.of(Map.of("productId", 100L, "quantity", 1))
        ), ctx);
        String draftId = String.valueOf(prepared.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        String token = String.valueOf(((Map<?, ?>) prepared.getData().get("draft")).get("confirmToken"));
        AiDraft draft = store.find(88L, 1L, draftId).orElseThrow();
        draft.setExpireAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        store.save(draft);
        AiDraftConfirmResult r = draftService.confirm(ctx, draftId, token);
        assertEquals("DRAFT_EXPIRED", r.getErrorCode());
    }

    @Test
    void outbound_otherUser_denied() {
        ToolResult prepared = outboundTool.execute(Map.of(
                "customerId", 10L,
                "warehouseId", 20L,
                "items", List.of(Map.of("productId", 100L, "quantity", 1))
        ), ctx);
        String draftId = String.valueOf(prepared.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        String token = String.valueOf(((Map<?, ?>) prepared.getData().get("draft")).get("confirmToken"));
        AiDraftConfirmResult r = draftService.confirm(otherUser, draftId, token);
        assertEquals("NOT_FOUND", r.getErrorCode());
    }

    @Test
    void outbound_otherTenant_denied() {
        ToolResult prepared = outboundTool.execute(Map.of(
                "customerId", 10L,
                "warehouseId", 20L,
                "items", List.of(Map.of("productId", 100L, "quantity", 1))
        ), ctx);
        String draftId = String.valueOf(prepared.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        String token = String.valueOf(((Map<?, ?>) prepared.getData().get("draft")).get("confirmToken"));
        AiDraftConfirmResult r = draftService.confirm(otherTenant, draftId, token);
        assertEquals("NOT_FOUND", r.getErrorCode());
    }

    @Test
    void outbound_permissionDenied() {
        ToolResult prepared = outboundTool.execute(Map.of(
                "customerId", 10L,
                "warehouseId", 20L,
                "items", List.of(Map.of("productId", 100L, "quantity", 1))
        ), ctx);
        String draftId = String.valueOf(prepared.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        String token = String.valueOf(((Map<?, ?>) prepared.getData().get("draft")).get("confirmToken"));
        AiExecutionContext denied = AiExecutionContext.builder()
                .userId(1L).tenantId(88L).username("u1").conversationId("c1")
                .permissions(Set.of("ck:outbound:list"))
                .build();
        AiDraftConfirmResult r = draftService.confirm(denied, draftId, token);
        assertEquals("PERMISSION_DENIED", r.getErrorCode());
    }

    @Test
    void outbound_businessFailure_keepsOpen() {
        ToolResult prepared = outboundTool.execute(Map.of(
                "customerId", 10L,
                "warehouseId", 20L,
                "items", List.of(Map.of("productId", 100L, "quantity", 1))
        ), ctx);
        String draftId = String.valueOf(prepared.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        String token = String.valueOf(((Map<?, ?>) prepared.getData().get("draft")).get("confirmToken"));
        when(outboundFacade.createProductionSaleOutBound(any()))
                .thenThrow(new jakarta.validation.ValidationException("库存不足"));
        AiDraftConfirmResult r = draftService.confirm(ctx, draftId, token);
        assertFalse(r.isSuccess());
        assertEquals("CREATE_FAILED", r.getErrorCode());
        assertEquals(DraftStatus.OPEN, r.getDraftStatus());
        AiDraft draft = store.find(88L, 1L, draftId).orElseThrow();
        assertEquals(DraftStatus.OPEN, draft.getStatus());
        assertNotNull(draft.getLastConfirmError());
    }

    @Test
    void inbound_draft_confirm_duplicate() {
        ToolResult prepared = inboundTool.execute(Map.of(
                "supplierKeyword", "供应商X",
                "warehouseId", 20L,
                "items", List.of(Map.of("sku", "A001", "quantity", 5))
        ), ctx);
        assertEquals("DRAFT", prepared.getData().get("responseType"));
        String draftId = String.valueOf(prepared.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        String token = String.valueOf(((Map<?, ?>) prepared.getData().get("draft")).get("confirmToken"));
        when(inboundFacade.create(any(InboundCreateReq.class))).thenReturn(true);
        assertTrue(draftService.confirm(ctx, draftId, token).isSuccess());
        assertEquals("ALREADY_CONFIRMED", draftService.confirm(ctx, draftId, token).getErrorCode());
        verify(inboundFacade, times(1)).create(any());
    }

    @Test
    void stocktake_draft_confirm_and_invalidScope() {
        ToolResult bad = stocktakeTool.execute(Map.of(
                "warehouseKeyword", "杭州一号仓"
        ), ctx);
        assertTrue(Boolean.TRUE.equals(bad.getData().get("needClarification")));
        assertTrue(bad.getMessage().contains("盘点范围") || bad.getMessage().contains("全仓"));

        ToolResult prepared = stocktakeTool.execute(Map.of(
                "warehouseId", 20L,
                "productKeywords", List.of("A001")
        ), ctx);
        assertEquals("DRAFT", prepared.getData().get("responseType"));
        String draftId = String.valueOf(prepared.getData().get("draftId"));
        @SuppressWarnings("unchecked")
        String token = String.valueOf(((Map<?, ?>) prepared.getData().get("draft")).get("confirmToken"));
        when(stockFacade.createStockTake(any(StockTakeCreateReq.class))).thenReturn(55L);
        AiDraftConfirmResult ok = draftService.confirm(ctx, draftId, token);
        assertTrue(ok.isSuccess());
        assertEquals(55L, ok.getBusinessOrderId());
    }

    @Test
    void l3_tools_not_in_llm_risk_policy_without_token() {
        // Natural language cannot create: L3 tools require confirmToken (covered by ToolRiskPolicy + registrar skip).
        assertEquals("create_outbound_order", new com.example.ai.tool.create.CreateOutboundOrderTool().name());
        assertEquals(com.example.ai.tool.ToolRiskLevel.L3_CONFIRM_REQUIRED,
                new com.example.ai.tool.create.CreateOutboundOrderTool().riskLevel());
    }
}
