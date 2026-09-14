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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PrepareStocktakeDraftTool extends AbstractDraftPrepareTool {

    @Resource
    private DraftEntityResolver resolver;
    @Resource
    private AiDraftService draftService;

    @Override
    public String name() {
        return "prepare_stocktake_draft";
    }

    @Override
    public String description() {
        return "生成盘点单草稿（L2）。必须明确盘点范围（商品/货架/批次），禁止默认全仓盘点。需 UI 确认后才创建。";
    }

    @Override
    public String requiredPermission() {
        return "ck:stocktake:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("warehouseId", Map.of("type", "integer"));
        props.put("warehouseKeyword", Map.of("type", "string"));
        props.put("takeType", Map.of("type", "integer", "description", "1动态 2静态，默认1"));
        props.put("takeScope", Map.of("type", "integer", "description", "2批次 3货架 4商品；禁止默认1全仓"));
        props.put("productIds", Map.of("type", "array"));
        props.put("productKeywords", Map.of("type", "array"));
        props.put("shelfIds", Map.of("type", "array"));
        props.put("batchIds", Map.of("type", "array"));
        props.put("remark", Map.of("type", "string"));
        props.put("parsedInput", Map.of("type", "string"));
        return schema(List.of(), props);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        List<String> warnings = new ArrayList<>();
        Map<String, Object> resolved = new LinkedHashMap<>();

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

        Integer takeScope = WmsQuerySupport.integer(arguments, "takeScope");
        List<String> productIds = toStringList(arguments.get("productIds"));
        List<String> shelfIds = toStringList(arguments.get("shelfIds"));
        List<String> batchIds = toStringList(arguments.get("batchIds"));

        // Resolve product keywords → ids
        Object rawKw = arguments.get("productKeywords");
        if (rawKw instanceof List<?> kws) {
            for (Object kw : kws) {
                if (kw == null) {
                    continue;
                }
                DraftEntityResolver.ResolveOutcome p = resolver.resolveProduct(context, null, String.valueOf(kw));
                if (p.kind() == DraftEntityResolver.Kind.AMBIGUOUS) {
                    return clarification(p.message(), p.candidates());
                }
                if (!p.ok()) {
                    return clarification(p.message() == null ? "请指定盘点商品" : p.message(), List.of());
                }
                productIds.add(String.valueOf(p.entity().get("productId")));
            }
        }

        if (takeScope == null) {
            if (!productIds.isEmpty()) {
                takeScope = 4;
            } else if (!shelfIds.isEmpty()) {
                takeScope = 3;
            } else if (!batchIds.isEmpty()) {
                takeScope = 2;
            } else {
                return clarification(
                        "请明确盘点范围：指定商品 / 货架 / 批次。系统不会默认全仓盘点。",
                        List.of());
            }
        }

        if (takeScope == 1) {
            return clarification("全仓盘点需业务侧明确授权范围；V1 请指定商品、货架或批次。", List.of());
        }
        if (takeScope == 4 && productIds.isEmpty()) {
            return clarification("商品盘点请指定 SKU/商品", List.of());
        }
        if (takeScope == 3 && shelfIds.isEmpty()) {
            return clarification("货架盘点请选择货架", List.of());
        }
        if (takeScope == 2 && batchIds.isEmpty()) {
            return clarification("批次盘点请选择批次", List.of());
        }

        Integer takeType = WmsQuerySupport.integer(arguments, "takeType");
        if (takeType == null) {
            takeType = 1;
        }

        Long warehouseId = ((Number) warehouse.entity().get("warehouseId")).longValue();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("warehouseId", warehouseId);
        payload.put("takeType", takeType);
        payload.put("takeScope", takeScope);
        payload.put("productIds", productIds);
        payload.put("shelfIds", shelfIds);
        payload.put("batchIds", batchIds);
        payload.put("remark", WmsQuerySupport.str(arguments, "remark"));

        Map<String, Object> display = new LinkedHashMap<>();
        display.put("draftTypeLabel", "盘点单");
        display.put("warehouseName", warehouse.entity().get("warehouseName"));
        display.put("warehouseId", warehouseId);
        display.put("takeType", takeType);
        display.put("takeScope", takeScope);
        display.put("takeScopeLabel", scopeLabel(takeScope));
        display.put("productIds", productIds);
        display.put("shelfIds", shelfIds);
        display.put("batchIds", batchIds);
        display.put("items", List.of());

        resolved.put("takeScope", takeScope);
        resolved.put("productIds", productIds);

        AiDraft draft = draftService.create(context, DraftType.STOCKTAKE,
                WmsQuerySupport.str(arguments, "parsedInput"), resolved, payload, display,
                warnings, DraftValidationResult.OK);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("responseType", "DRAFT");
        data.put("draft", AiDraftViews.toCard(draft));
        data.put("draftId", draft.getDraftId());
        data.put("draftType", DraftType.STOCKTAKE.name());
        data.put("status", DraftStatus.OPEN.name());
        return ToolResult.ok(data, "已生成盘点单草稿，请在卡片上确认创建");
    }

    private static String scopeLabel(int scope) {
        return switch (scope) {
            case 2 -> "批次";
            case 3 -> "货架";
            case 4 -> "商品";
            default -> String.valueOf(scope);
        };
    }

    private static List<String> toStringList(Object raw) {
        List<String> out = new ArrayList<>();
        if (!(raw instanceof List<?> list)) {
            return out;
        }
        for (Object o : list) {
            if (o != null) {
                out.add(String.valueOf(o));
            }
        }
        return out;
    }
}
