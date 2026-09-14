package com.example.ai.tool.query.inventory;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiBatchStock;
import com.example.ai.tool.query.dto.AiQueryDtos.AiShelfStock;
import com.example.Facade.CkInventoryFacade;
import com.example.entity.cangku.resp.InventoryBatchResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetInventoryDetailTool extends AbstractWmsQueryTool {

    @Resource
    private CkInventoryFacade inventoryFacade;

    @Override
    public String name() {
        return "get_inventory_detail";
    }

    @Override
    public String description() {
        return "查询商品在指定仓库的批次/货架库存明细（复用 CkInventoryFacade.batches）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:inventory:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("productId", Map.of("type", "integer"));
        props.put("warehouseId", Map.of("type", "integer"));
        return schema(List.of("productId", "warehouseId"), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        Long productId = WmsQuerySupport.lng(arguments, "productId");
        Long warehouseId = WmsQuerySupport.lng(arguments, "warehouseId");
        if (productId == null || warehouseId == null) {
            throw new AiValidationException("需要 productId 与 warehouseId");
        }
        List<InventoryBatchResp> batches = inventoryFacade.batches(warehouseId, productId, context.getTenantId());
        if (batches == null || batches.isEmpty()) {
            return ToolResult.fail(ToolErrorCode.NOT_FOUND, "未找到批次库存明细");
        }
        List<AiBatchStock> items = new ArrayList<>();
        for (InventoryBatchResp b : batches) {
            List<AiShelfStock> shelves = new ArrayList<>();
            if (b.getShelfList() != null) {
                for (InventoryBatchResp.ShelfInfo s : b.getShelfList()) {
                    shelves.add(AiShelfStock.builder()
                            .shelfId(s.getShelfId()).shelfName(s.getShelfName()).quantity(s.getQuantity())
                            .build());
                }
            }
            items.add(AiBatchStock.builder()
                    .batchNo(b.getBatchNo()).quantity(b.getQuantity()).shelves(shelves)
                    .build());
        }
        return ToolResult.ok(Map.of(
                "productId", productId,
                "warehouseId", warehouseId,
                "batches", items,
                "fieldSource", "CkInventoryFacade.batches"
        ), "库存明细查询完成");
    }
}
