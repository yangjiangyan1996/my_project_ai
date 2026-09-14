package com.example.ai.tool.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.audit.LoggingAiAuditRecorder;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.permission.AiPermissionChecker;
import com.example.ai.permission.AiToolPermissionGuard;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolExecutor;
import com.example.ai.tool.ToolInputValidator;
import com.example.ai.tool.ToolRegistry;
import com.example.ai.tool.ToolRequest;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.ToolRiskPolicy;
import com.example.ai.tool.query.inventory.GetInventoryTool;
import com.example.ai.tool.query.product.GetProductTool;
import com.example.ai.tool.query.product.SearchProductTool;
import com.example.ai.tool.query.warehouse.GetWarehouseTool;
import com.example.ai.tool.query.warehouse.SearchWarehouseTool;
import com.example.ai.tool.query.inbound.GetInboundOrderTool;
import com.example.ai.tool.query.inbound.ListPendingInboundsTool;
import com.example.ai.tool.query.outbound.GetOutboundOrderTool;
import com.example.ai.tool.query.outbound.ListPendingOutboundsTool;
import com.example.ai.tool.query.stocktake.GetStocktakeOrderTool;
import com.example.ai.tool.query.stocktake.ListPendingStocktakesTool;
import com.example.Facade.CKProductFacade;
import com.example.Facade.CkInboundFacade;
import com.example.Facade.CkInventoryFacade;
import com.example.Facade.CkOutboundFacade;
import com.example.Facade.CkStockFacade;
import com.example.Facade.CkWarehouseFacade;
import com.example.entity.cangku.req.InboundListPageReq;
import com.example.entity.cangku.req.InventoryListPageReq;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.entity.cangku.req.ProductListPageReq;
import com.example.entity.cangku.req.StockListPageReq;
import com.example.entity.cangku.req.WareHouseListPageReq;
import com.example.entity.cangku.resp.InboundDetailResp;
import com.example.entity.cangku.resp.InboundListPageResp;
import com.example.entity.cangku.resp.InventoryPageListResp;
import com.example.entity.cangku.resp.OutboundDetailResp;
import com.example.entity.cangku.resp.OutboundListPageResp;
import com.example.entity.cangku.resp.ProductPageListResp;
import com.example.entity.cangku.resp.ProductSimpleListResp;
import com.example.entity.cangku.resp.StockDetailResp;
import com.example.entity.cangku.resp.StockListPageResp;
import com.example.entity.cangku.resp.WareHouseResp;
import com.example.enums.CkInOutboundEnums;
import com.example.enums.CkStockTakeEnums;
import com.example.service.CkWareHouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Phase C WMS query tools — mocked Facades only (no DEV_DB, no real DeepSeek).
 */
class WmsQueryToolsPhaseCTest {

    private ToolRegistry registry;
    private ToolExecutor executor;
    private AiExecutionContext ctxTenant88;

    private CKProductFacade productFacade;
    private CkWarehouseFacade warehouseFacade;
    private CkWareHouseService wareHouseService;
    private CkInventoryFacade inventoryFacade;
    private CkInboundFacade inboundFacade;
    private CkOutboundFacade outboundFacade;
    private CkStockFacade stockFacade;

    @BeforeEach
    void setUp() {
        registry = new ToolRegistry();
        productFacade = mock(CKProductFacade.class);
        warehouseFacade = mock(CkWarehouseFacade.class);
        wareHouseService = mock(CkWareHouseService.class);
        inventoryFacade = mock(CkInventoryFacade.class);
        inboundFacade = mock(CkInboundFacade.class);
        outboundFacade = mock(CkOutboundFacade.class);
        stockFacade = mock(CkStockFacade.class);

        SearchProductTool searchProduct = new SearchProductTool();
        ReflectionTestUtils.setField(searchProduct, "productFacade", productFacade);
        GetProductTool getProduct = new GetProductTool();
        ReflectionTestUtils.setField(getProduct, "productFacade", productFacade);
        SearchWarehouseTool searchWh = new SearchWarehouseTool();
        ReflectionTestUtils.setField(searchWh, "warehouseFacade", warehouseFacade);
        GetWarehouseTool getWh = new GetWarehouseTool();
        ReflectionTestUtils.setField(getWh, "warehouseFacade", warehouseFacade);
        ReflectionTestUtils.setField(getWh, "wareHouseService", wareHouseService);
        GetInventoryTool getInv = new GetInventoryTool();
        ReflectionTestUtils.setField(getInv, "inventoryFacade", inventoryFacade);
        ReflectionTestUtils.setField(getInv, "productFacade", productFacade);
        GetInboundOrderTool getIn = new GetInboundOrderTool();
        ReflectionTestUtils.setField(getIn, "inboundFacade", inboundFacade);
        ListPendingInboundsTool pendingIn = new ListPendingInboundsTool();
        ReflectionTestUtils.setField(pendingIn, "inboundFacade", inboundFacade);
        GetOutboundOrderTool getOut = new GetOutboundOrderTool();
        ReflectionTestUtils.setField(getOut, "outboundFacade", outboundFacade);
        ListPendingOutboundsTool pendingOut = new ListPendingOutboundsTool();
        ReflectionTestUtils.setField(pendingOut, "outboundFacade", outboundFacade);
        GetStocktakeOrderTool getSt = new GetStocktakeOrderTool();
        ReflectionTestUtils.setField(getSt, "stockFacade", stockFacade);
        ListPendingStocktakesTool pendingSt = new ListPendingStocktakesTool();
        ReflectionTestUtils.setField(pendingSt, "stockFacade", stockFacade);

        for (var t : List.of(searchProduct, getProduct, searchWh, getWh, getInv,
                getIn, pendingIn, getOut, pendingOut, getSt, pendingSt)) {
            registry.register(t);
        }

        AiToolPermissionGuard guard = new AiToolPermissionGuard();
        ReflectionTestUtils.setField(guard, "permissionChecker", new AiPermissionChecker());
        executor = new ToolExecutor();
        ReflectionTestUtils.setField(executor, "toolRegistry", registry);
        ReflectionTestUtils.setField(executor, "toolRiskPolicy", new ToolRiskPolicy());
        ReflectionTestUtils.setField(executor, "permissionGuard", guard);
        ReflectionTestUtils.setField(executor, "inputValidator", new ToolInputValidator());
        ReflectionTestUtils.setField(executor, "auditRecorder", new LoggingAiAuditRecorder());

        ctxTenant88 = AiExecutionContext.builder()
                .userId(1L).tenantId(88L).username("u")
                .conversationId("c").requestId("r")
                .permissions(Set.of())
                .build();
    }

    private ToolResult exec(String name, Map<String, Object> args) {
        return executor.execute(ToolRequest.builder()
                .toolName(name).arguments(args).executionContext(ctxTenant88).build());
    }

    @Test
    void searchProduct_tenantScopedAndPassesTenantToFacade() {
        ProductSimpleListResp p = new ProductSimpleListResp();
        p.setId(10L);
        p.setSku("A001");
        p.setName("螺丝");
        when(productFacade.productSimpleList(any(), eq("A001"))).thenReturn(List.of(p));
        when(productFacade.pageList(any(), any(ProductListPageReq.class))).thenAnswer(inv -> {
            ProductListPageReq req = inv.getArgument(1);
            assertEquals(88L, req.getTenantId());
            return new Page<>(1, 20);
        });

        ToolResult r = exec("search_product", Map.of("keyword", "A001"));
        assertTrue(r.isSuccess());
        verify(productFacade).productSimpleList(argThat(u -> Long.valueOf(88L).equals(u.getTenantId())), eq("A001"));
    }

    @Test
    void getProduct_notFound() {
        when(productFacade.detail(eq(999L), any())).thenThrow(
                new jakarta.validation.ValidationException("商品不存在"));
        ToolResult r = exec("get_product", Map.of("productId", 999));
        assertFalse(r.isSuccess());
        assertEquals(ToolErrorCode.NOT_FOUND.name(), r.getErrorCode());
    }

    @Test
    void searchWarehouse_usesFacadeList() {
        WareHouseResp w = new WareHouseResp();
        w.setId(1L);
        w.setName("杭州仓");
        w.setCode("HZ");
        when(warehouseFacade.listOfWareHouse(any(), any(WareHouseListPageReq.class))).thenAnswer(inv -> {
            WareHouseListPageReq req = inv.getArgument(1);
            assertEquals(88L, req.getTenantId());
            Page<WareHouseResp> page = new Page<>(1, 20);
            page.setRecords(List.of(w));
            page.setTotal(1);
            return page;
        });
        ToolResult r = exec("search_warehouse", Map.of("keyword", "杭州"));
        assertTrue(r.isSuccess());
    }

    @Test
    void getWarehouse_notFoundOtherTenant() {
        when(warehouseFacade.listEnable(any())).thenReturn(List.of());
        when(wareHouseService.selectByTenantIdAndWareHouseId(88L, 7L)).thenReturn(null);
        ToolResult r = exec("get_warehouse", Map.of("warehouseId", 7));
        assertFalse(r.isSuccess());
        assertEquals(ToolErrorCode.NOT_FOUND.name(), r.getErrorCode());
    }

    @Test
    void getInventory_returnsFacadeFieldsWithoutRecompute() {
        ProductPageListResp product = new ProductPageListResp();
        product.setId(5L);
        product.setSku("SKU-1");
        when(productFacade.detail(eq(5L), any())).thenReturn(product);

        InventoryPageListResp.WarehouseInventory wi = new InventoryPageListResp.WarehouseInventory();
        wi.setWarehouseId(2L);
        wi.setWarehouseName("主仓");
        wi.setQuantity(new BigDecimal("100"));
        wi.setLockedQuantity(new BigDecimal("10"));
        wi.setAvailableQuantity(new BigDecimal("90"));

        InventoryPageListResp row = new InventoryPageListResp();
        row.setProductId(5L);
        row.setSku("SKU-1");
        row.setProductName("商品1");
        row.setWarehouseInventoryList(List.of(wi));

        when(inventoryFacade.pageList(any(), any(InventoryListPageReq.class))).thenAnswer(inv -> {
            InventoryListPageReq req = inv.getArgument(1);
            assertEquals(88L, req.getTenantId());
            assertEquals("SKU-1", req.getSku());
            Page<InventoryPageListResp> page = new Page<>(1, 50);
            page.setRecords(List.of(row));
            return page;
        });

        ToolResult r = exec("get_inventory", Map.of("productId", 5));
        assertTrue(r.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) r.getData();
        assertNotNull(data.get("items"));
    }

    @Test
    void listPendingInbounds_usesWaitAuditStatus() {
        when(inboundFacade.pageList(any(), any(InboundListPageReq.class))).thenAnswer(inv -> {
            InboundListPageReq req = inv.getArgument(1);
            assertEquals(Integer.valueOf(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode()), req.getStatus());
            assertEquals(88L, req.getTenantId());
            Page<InboundListPageResp> page = new Page<>(1, 20);
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        });
        assertTrue(exec("list_pending_inbounds", Map.of()).isSuccess());
    }

    @Test
    void getInboundOrder_notFound() {
        when(inboundFacade.pageList(any(), any(InboundListPageReq.class))).thenReturn(new Page<>(1, 50));
        ToolResult r = exec("get_inbound_order", Map.of("orderNo", "NOPE"));
        assertEquals(ToolErrorCode.NOT_FOUND.name(), r.getErrorCode());
    }

    @Test
    void getInboundOrder_byId() {
        InboundDetailResp detail = new InboundDetailResp();
        detail.setId(3L);
        detail.setOrderNo("IN-1");
        detail.setStatus(1);
        when(inboundFacade.detail(3L, 88L)).thenReturn(detail);
        ToolResult r = exec("get_inbound_order", Map.of("orderId", 3));
        assertTrue(r.isSuccess());
        verify(inboundFacade).detail(3L, 88L);
    }

    @Test
    void listPendingOutbounds_usesWaitAudit() {
        when(outboundFacade.pageList(any(), any(OutboundListPageReq.class))).thenAnswer(inv -> {
            OutboundListPageReq req = inv.getArgument(1);
            assertEquals(Integer.valueOf(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode()), req.getStatus());
            return new Page<>(1, 20);
        });
        assertTrue(exec("list_pending_outbounds", Map.of()).isSuccess());
    }

    @Test
    void getOutboundOrder_detail() {
        OutboundDetailResp d = new OutboundDetailResp();
        d.setId(9L);
        d.setOrderNo("OUT-9");
        d.setStatus(1);
        when(outboundFacade.detail(9L, 88L)).thenReturn(d);
        assertTrue(exec("get_outbound_order", Map.of("orderId", 9)).isSuccess());
    }

    @Test
    void listPendingStocktakes_filtersUnderReview() {
        StockListPageResp pending = new StockListPageResp();
        pending.setId(1L);
        pending.setStockTakeNo("ST-1");
        pending.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW.getCode());
        StockListPageResp other = new StockListPageResp();
        other.setId(2L);
        other.setStockTakeNo("ST-2");
        other.setApprovalStatus(CkStockTakeEnums.ApprovalStatus.APPROVED.getCode());

        when(stockFacade.pageList(any(), any(StockListPageReq.class))).thenAnswer(inv -> {
            StockListPageReq req = inv.getArgument(1);
            assertEquals(88L, req.getTenantId());
            Page<StockListPageResp> page = new Page<>(1, 50);
            page.setRecords(List.of(pending, other));
            return page;
        });
        ToolResult r = exec("list_pending_stocktakes", Map.of("limit", 20));
        assertTrue(r.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) r.getData();
        assertEquals(1, ((List<?>) data.get("items")).size());
    }

    @Test
    void getStocktake_notFoundMappedFromNpe() {
        when(stockFacade.stockDetail(1L, 88L)).thenThrow(new NullPointerException());
        ToolResult r = exec("get_stocktake_order", Map.of("stockTakeId", 1));
        assertEquals(ToolErrorCode.NOT_FOUND.name(), r.getErrorCode());
    }

    @Test
    void getStocktake_ok() {
        StockDetailResp d = new StockDetailResp();
        d.setId(1L);
        d.setStockTakeNo("ST-1");
        when(stockFacade.stockDetail(1L, 88L)).thenReturn(d);
        assertTrue(exec("get_stocktake_order", Map.of("stockTakeId", 1)).isSuccess());
    }

    @Test
    void sideEffectGate_toolsDoNotImportMapperOrRepository() throws Exception {
        Path root = Path.of("src/main/java/com/example/ai/tool/query");
        assertTrue(Files.isDirectory(root), "query tools package missing");
        try (Stream<Path> files = Files.walk(root)) {
            List<Path> javaFiles = files.filter(p -> p.toString().endsWith(".java")).toList();
            assertFalse(javaFiles.isEmpty());
            for (Path file : javaFiles) {
                String src = Files.readString(file);
                assertFalse(src.contains("import com.example.mapper"), "Mapper import in " + file);
                assertFalse(src.contains(".mapper."), "mapper package in " + file);
                assertFalse(src.contains("InventoryHolder"), "InventoryHolder in " + file);
                assertFalse(src.contains("JdbcTemplate"), "JdbcTemplate in " + file);
            }
        }
    }

    @Test
    void catalogNamesPresent() {
        assertTrue(registry.contains("search_product"));
        assertTrue(registry.contains("get_product"));
        assertTrue(registry.contains("search_warehouse"));
        assertTrue(registry.contains("get_warehouse"));
        assertTrue(registry.contains("get_inventory"));
        assertTrue(registry.contains("get_inbound_order"));
        assertTrue(registry.contains("list_pending_inbounds"));
        assertTrue(registry.contains("get_outbound_order"));
        assertTrue(registry.contains("list_pending_outbounds"));
        assertTrue(registry.contains("get_stocktake_order"));
        assertTrue(registry.contains("list_pending_stocktakes"));
    }
}
