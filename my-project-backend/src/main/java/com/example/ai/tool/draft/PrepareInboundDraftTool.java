package com.example.ai.tool.draft;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.draft.AiDraft;
import com.example.ai.draft.AiDraftService;
import com.example.ai.draft.AiDraftViews;
import com.example.ai.draft.DraftEntityResolver;
import com.example.ai.draft.DraftStatus;
import com.example.ai.draft.DraftType;
import com.example.ai.draft.DraftValidationResult;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.entity.cangku.resp.ShelfEnalbedListResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PrepareInboundDraftTool extends AbstractDraftPrepareTool {

    @Resource
    private DraftEntityResolver resolver;
    @Resource
    private AiDraftService draftService;

    @Override
    public String name() {
        return "prepare_inbound_draft";
    }

    @Override
    public String description() {
        return "根据供应商、仓库、商品与数量生成采购入库单草稿（L2）。不会创建真实单据；需 UI 确认创建。";
    }

    @Override
    public String requiredPermission() {
        return "ck:inbound:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("supplierId", Map.of("type", "integer"));
        props.put("supplierKeyword", Map.of("type", "string"));
        props.put("warehouseId", Map.of("type", "integer"));
        props.put("warehouseKeyword", Map.of("type", "string"));
        props.put("remark", Map.of("type", "string"));
        props.put("items", Map.of("type", "array", "items", Map.of("type", "object")));
        props.put("parsedInput", Map.of("type", "string"));
        return schema(List.of("items"), props);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        List<String> warnings = new ArrayList<>();
        Map<String, Object> resolved = new LinkedHashMap<>();

        DraftEntityResolver.ResolveOutcome supplier = resolver.resolveSupplier(
                context, WmsQuerySupport.lng(arguments, "supplierId"),
                WmsQuerySupport.str(arguments, "supplierKeyword"));
        if (supplier.kind() == DraftEntityResolver.Kind.AMBIGUOUS) {
            return clarification(supplier.message(), supplier.candidates());
        }
        if (!supplier.ok()) {
            return clarification(supplier.message() == null ? "请选择供应商" : supplier.message(), List.of());
        }
        resolved.put("supplier", supplier.entity());

        DraftEntityResolver.ResolveOutcome warehouse = resolver.resolveWarehouse(
                context, WmsQuerySupport.lng(arguments, "warehouseId"),
                WmsQuerySupport.str(arguments, "warehouseKeyword"));
        if (warehouse.kind() == DraftEntityResolver.Kind.AMBIGUOUS) {
            return clarification(warehouse.message(), warehouse.candidates());
        }
        if (!warehouse.ok()) {
            return clarification(warehouse.message() == null ? "请选择仓库" : warehouse.message(), List.of());
        }
        if (warehouse.autoWarning() != null) {
            warnings.add(warehouse.autoWarning());
        }
        resolved.put("warehouse", warehouse.entity());

        Object rawItems = arguments.get("items");
        if (!(rawItems instanceof List<?> itemList) || itemList.isEmpty()) {
            return clarification("请提供入库商品明细", List.of());
        }

        Long warehouseId = ((Number) warehouse.entity().get("warehouseId")).longValue();
        List<ShelfEnalbedListResp> shelves = resolver.shelves(context.getTenantId(), warehouseId);
        if (shelves == null || shelves.isEmpty()) {
            return clarification("该仓库没有可用货架，无法生成入库草稿", List.of());
        }
        ShelfEnalbedListResp defaultShelf = shelves.get(0);
        warnings.add("已使用默认货架：" + nullSafe(defaultShelf.getShelfName()) + "（确认前可修改后重新生成）");

        List<Map<String, Object>> displayItems = new ArrayList<>();
        List<Map<String, Object>> payloadItems = new ArrayList<>();
        double totalQty = 0;

        for (Object raw : itemList) {
            if (!(raw instanceof Map<?, ?> m)) {
                continue;
            }
            Map<String, Object> row = (Map<String, Object>) m;
            Long productId = WmsQuerySupport.lng(row, "productId");
            String skuOrKw = PrepareOutboundDraftTool.firstNonBlank(
                    WmsQuerySupport.str(row, "sku"),
                    WmsQuerySupport.str(row, "keyword"),
                    WmsQuerySupport.str(row, "productKeyword"));
            BigDecimal qtyBd = PrepareOutboundDraftTool.toDecimal(row.get("quantity"));
            if (qtyBd == null || qtyBd.compareTo(BigDecimal.ZERO) <= 0) {
                return clarification("商品数量必须大于 0", List.of());
            }
            double qty = qtyBd.doubleValue();
            DraftEntityResolver.ResolveOutcome product = resolver.resolveProduct(context, productId, skuOrKw);
            if (product.kind() == DraftEntityResolver.Kind.AMBIGUOUS) {
                return clarification(product.message(), product.candidates());
            }
            if (!product.ok()) {
                return clarification(product.message() == null ? "请指定商品" : product.message(), List.of());
            }

            String batchNo = PrepareOutboundDraftTool.firstNonBlank(
                    WmsQuerySupport.str(row, "batchNo"),
                    "AI" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                            + ThreadLocalRandom.current().nextInt(100, 999));

            Map<String, Object> display = new LinkedHashMap<>(product.entity());
            display.put("quantity", qty);
            display.put("batchNo", batchNo);
            display.put("shelfName", defaultShelf.getShelfName());
            displayItems.add(display);

            Map<String, Object> shelfAlloc = new LinkedHashMap<>();
            shelfAlloc.put("shelfLocationId", defaultShelf.getId());
            shelfAlloc.put("quantity", qty);

            Map<String, Object> payloadItem = new LinkedHashMap<>();
            payloadItem.put("productId", product.entity().get("productId"));
            payloadItem.put("productName", product.entity().get("productName"));
            payloadItem.put("sku", product.entity().get("sku"));
            payloadItem.put("spec", product.entity().get("spec"));
            payloadItem.put("unit", product.entity().get("unit"));
            payloadItem.put("actualQuantity", qty);
            payloadItem.put("batchNo", batchNo);
            payloadItem.put("priceUnit", BigDecimal.ONE);
            payloadItem.put("priceTotal", qtyBd);
            payloadItem.put("shelfAllocations", List.of(shelfAlloc));
            payloadItems.add(payloadItem);
            totalQty += qty;
        }

        if (payloadItems.isEmpty()) {
            return clarification("请提供有效商品明细", List.of());
        }

        String orderNo = PrepareOutboundDraftTool.generateOrderNo("RK");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderNo", orderNo);
        payload.put("orderType", 1); // purchase inbound
        payload.put("warehouseId", warehouseId);
        payload.put("supplierId", ((Number) supplier.entity().get("supplierId")).longValue());
        payload.put("status", 0);
        payload.put("remark", WmsQuerySupport.str(arguments, "remark"));
        payload.put("expectedDate", LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        payload.put("items", payloadItems);
        payload.put("totalQuantity", totalQty);
        payload.put("totalAmount", BigDecimal.valueOf(totalQty));

        Map<String, Object> display = new LinkedHashMap<>();
        display.put("draftTypeLabel", "入库单");
        display.put("supplierName", supplier.entity().get("supplierName"));
        display.put("supplierId", supplier.entity().get("supplierId"));
        display.put("warehouseName", warehouse.entity().get("warehouseName"));
        display.put("warehouseId", warehouseId);
        display.put("orderNoPreview", orderNo);
        display.put("items", displayItems);
        display.put("totalQuantity", totalQty);

        AiDraft draft = draftService.create(context, DraftType.INBOUND,
                WmsQuerySupport.str(arguments, "parsedInput"), resolved, payload, display,
                warnings, DraftValidationResult.WARN);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("responseType", "DRAFT");
        data.put("draft", AiDraftViews.toCard(draft));
        data.put("draftId", draft.getDraftId());
        data.put("draftType", DraftType.INBOUND.name());
        data.put("status", DraftStatus.OPEN.name());
        return ToolResult.ok(data, "已生成入库单草稿，请在卡片上确认创建");
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
