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
public class PrepareOutboundDraftTool extends AbstractDraftPrepareTool {

    @Resource
    private DraftEntityResolver resolver;
    @Resource
    private AiDraftService draftService;

    @Override
    public String name() {
        return "prepare_outbound_draft";
    }

    @Override
    public String description() {
        return "根据已解析的客户、仓库、商品与数量生成销售出库单草稿（L2）。不会创建真实单据；需用户在 UI 点击确认创建。";
    }

    @Override
    public String requiredPermission() {
        return "ck:outbound:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("customerId", Map.of("type", "integer"));
        props.put("customerKeyword", Map.of("type", "string"));
        props.put("warehouseId", Map.of("type", "integer"));
        props.put("warehouseKeyword", Map.of("type", "string"));
        props.put("remark", Map.of("type", "string"));
        props.put("items", Map.of(
                "type", "array",
                "description", "明细：productId 或 sku/keyword + quantity",
                "items", Map.of("type", "object")
        ));
        props.put("parsedInput", Map.of("type", "string"));
        return schema(List.of("items"), props);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        List<String> warnings = new ArrayList<>();
        Map<String, Object> resolved = new LinkedHashMap<>();

        DraftEntityResolver.ResolveOutcome customer = resolver.resolveCustomer(
                context, WmsQuerySupport.lng(arguments, "customerId"),
                WmsQuerySupport.str(arguments, "customerKeyword"));
        if (customer.kind() == DraftEntityResolver.Kind.AMBIGUOUS) {
            return clarification(customer.message(), customer.candidates());
        }
        if (!customer.ok()) {
            return clarification(customer.message() == null ? "请选择客户" : customer.message(), List.of());
        }
        resolved.put("customer", customer.entity());

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
            return clarification("请提供出库商品明细（SKU 与数量）", List.of());
        }

        Long warehouseId = ((Number) warehouse.entity().get("warehouseId")).longValue();
        List<Map<String, Object>> displayItems = new ArrayList<>();
        List<Map<String, Object>> payloadItems = new ArrayList<>();
        BigDecimal totalQty = BigDecimal.ZERO;
        DraftValidationResult validation = DraftValidationResult.OK;

        for (Object raw : itemList) {
            if (!(raw instanceof Map<?, ?> m)) {
                continue;
            }
            Map<String, Object> row = (Map<String, Object>) m;
            Long productId = WmsQuerySupport.lng(row, "productId");
            String skuOrKw = firstNonBlank(WmsQuerySupport.str(row, "sku"), WmsQuerySupport.str(row, "keyword"),
                    WmsQuerySupport.str(row, "productKeyword"));
            BigDecimal qty = toDecimal(row.get("quantity"));
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                return clarification("商品数量必须大于 0", List.of());
            }
            DraftEntityResolver.ResolveOutcome product = resolver.resolveProduct(context, productId, skuOrKw);
            if (product.kind() == DraftEntityResolver.Kind.AMBIGUOUS) {
                return clarification(product.message(), product.candidates());
            }
            if (!product.ok()) {
                return clarification(product.message() == null ? "请指定商品" : product.message(), List.of());
            }
            Long pid = ((Number) product.entity().get("productId")).longValue();
            List<Map<String, Object>> allocations =
                    resolver.allocateOutboundBatches(warehouseId, pid, context.getTenantId(), qty);
            BigDecimal allocated = allocations.stream()
                    .map(a -> toDecimal(a.get("quantity")))
                    .filter(q -> q != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (allocated.compareTo(qty) < 0) {
                warnings.add("库存检查：商品 "
                        + product.entity().get("sku")
                        + " 可分配批次/货架数量 " + allocated + "，需求 " + qty
                        + "（未自行判定绝对可出，以业务创建校验为准）");
                validation = DraftValidationResult.WARN;
                if (allocations.isEmpty()) {
                    validation = DraftValidationResult.BLOCK;
                    warnings.add("商品 " + product.entity().get("sku") + " 无法分配批次/货架，无法确认创建");
                }
            }

            Map<String, Object> display = new LinkedHashMap<>(product.entity());
            display.put("quantity", qty);
            display.put("allocatedQuantity", allocated);
            displayItems.add(display);

            Map<String, Object> payloadItem = new LinkedHashMap<>();
            payloadItem.put("productId", pid);
            payloadItem.put("productName", product.entity().get("productName"));
            payloadItem.put("sku", product.entity().get("sku"));
            payloadItem.put("spec", product.entity().get("spec"));
            payloadItem.put("unit", product.entity().get("unit"));
            payloadItem.put("quantity", qty);
            payloadItem.put("price", BigDecimal.ZERO);
            payloadItem.put("priceTotal", BigDecimal.ZERO);
            payloadItem.put("priceUnitUsd", BigDecimal.ZERO);
            payloadItem.put("priceTotalUsd", BigDecimal.ZERO);
            payloadItem.put("batchAllocations", allocations);
            payloadItems.add(payloadItem);
            totalQty = totalQty.add(qty);
        }

        if (payloadItems.isEmpty()) {
            return clarification("请提供有效商品明细", List.of());
        }

        String orderNo = generateOrderNo("CK");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderNo", orderNo);
        payload.put("orderType", 1); // sale outbound typical
        payload.put("warehouseId", warehouseId);
        payload.put("customerId", ((Number) customer.entity().get("customerId")).longValue());
        payload.put("expectedDate", LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        payload.put("remark", WmsQuerySupport.str(arguments, "remark"));
        payload.put("status", 0); // wait submit — never auto approve
        payload.put("items", payloadItems);
        payload.put("totalQuantity", totalQty);
        payload.put("totalAmount", BigDecimal.ZERO);
        payload.put("totalAmountUsd", BigDecimal.ZERO);

        Map<String, Object> display = new LinkedHashMap<>();
        display.put("draftTypeLabel", "出库单");
        display.put("customerName", customer.entity().get("customerName"));
        display.put("customerId", customer.entity().get("customerId"));
        display.put("warehouseName", warehouse.entity().get("warehouseName"));
        display.put("warehouseId", warehouseId);
        display.put("orderNoPreview", orderNo);
        display.put("items", displayItems);
        display.put("totalQuantity", totalQty);

        String parsed = WmsQuerySupport.str(arguments, "parsedInput");
        AiDraft draft = draftService.create(context, DraftType.OUTBOUND, parsed, resolved, payload, display,
                warnings, validation);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("responseType", "DRAFT");
        data.put("draft", AiDraftViews.toCard(draft));
        data.put("draftId", draft.getDraftId());
        data.put("draftType", DraftType.OUTBOUND.name());
        data.put("status", DraftStatus.OPEN.name());
        return ToolResult.ok(data, "已生成出库单草稿，请在卡片上确认创建");
    }

    static String generateOrderNo(String prefix) {
        LocalDate d = LocalDate.now();
        int r = ThreadLocalRandom.current().nextInt(1000, 9999);
        return prefix + d.format(DateTimeFormatter.BASIC_ISO_DATE) + r;
    }

    static BigDecimal toDecimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(v).trim());
        } catch (Exception e) {
            return null;
        }
    }

    static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
