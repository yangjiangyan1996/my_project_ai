package com.example.ai.tool.create;

import com.example.ai.draft.DraftType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CreateStocktakeOrderTool extends AbstractL3CreateTool {

    @Override
    public String name() {
        return "create_stocktake_order";
    }

    @Override
    public String description() {
        return "确认创建盘点单（L3）。仅允许 Draft Confirm 流程调用，禁止自然语言直接执行。";
    }

    @Override
    public String requiredPermission() {
        return "ck:stocktake:create";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("draftId", Map.of("type", "string"));
        return schema(List.of("draftId"), props);
    }

    @Override
    protected DraftType expectedType() {
        return DraftType.STOCKTAKE;
    }
}
