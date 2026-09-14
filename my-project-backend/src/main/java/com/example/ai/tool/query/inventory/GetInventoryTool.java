package com.example.ai.tool.query.inventory;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiInventoryResult;
import com.example.ai.tool.query.dto.AiQueryDtos.AiShelfStock;
import com.example.ai.tool.query.dto.AiQueryDtos.AiWarehouseStock;
import com.example.Facade.CKProductFacade;
import com.example.Facade.CkInventoryFacade;
import com.example.entity.cangku.req.InventoryListPageReq;
import com.example.entity.cangku.resp.InventoryPageListResp;
import com.example.entity.cangku.resp.ProductPageListResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns Facade fields as-is. Does NOT compute available = quantity - locked.
 */
@Component
public class GetInventoryTool extends AbstractWmsQueryTool {

    @Resource
    private CkInventoryFacade inventoryFacade;
    @Resource
    private CKProductFacade productFacade;

    @Override
    public String name() {
        return "get_inventory";
    }

    @Override
    public String description() {
        return "查询商品库存。输入 productId 或 sku；可选 warehouseId。返回系统真实 quantity/lockedQuantity（不自行计算可用量）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:inventory:list";
    }

    @Override
    public long timeoutMs() {
        return 5000L;
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("productId", Map.of("type", "integer"));
        props.put("sku", Map.of("type", "string"));
        props.put("warehouseId", Map.of("type", "integer"));
        return schema(List.of(), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        Long productId = WmsQuerySupport.lng(arguments, "productId");
        String sku = WmsQuerySupport.str(arguments, "sku");
        Long warehouseId = WmsQuerySupport.lng(arguments, "warehouseId");
        if (productId == null && sku == null) {
            throw new AiValidationException("需要提供 productId 或 sku");
        }

        if (productId != null && sku == null) {
            ProductPageListResp product = productFacade.detail(productId, WmsQuerySupport.userInfo(context));
            if (product == null) {
                return ToolResult.fail(ToolErrorCode.NOT_FOUND, "商品不存在");
            }
            sku = product.getSku();
        }

        InventoryListPageReq req = new InventoryListPageReq();
        req.setTenantId(context.getTenantId());
        req.setUserId(context.getUserId());
        req.setSku(sku);
        req.setPage(1);
        req.setSize(WmsQuerySupport.MAX_LIMIT);
        Page<InventoryPageListResp> page = inventoryFacade.pageList(WmsQuerySupport.page(WmsQuerySupport.MAX_LIMIT), req);
        if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
            return ToolResult.fail(ToolErrorCode.NOT_FOUND, "未找到库存记录");
        }

        List<AiInventoryResult> results = new ArrayList<>();
        for (InventoryPageListResp row : page.getRecords()) {
            if (productId != null && !productId.equals(row.getProductId())) {
                continue;
            }
            List<AiWarehouseStock> warehouses = new ArrayList<>();
            if (row.getWarehouseInventoryList() != null) {
                for (InventoryPageListResp.WarehouseInventory w : row.getWarehouseInventoryList()) {
                    if (warehouseId != null && !warehouseId.equals(w.getWarehouseId())) {
                        continue;
                    }
                    warehouses.add(AiWarehouseStock.builder()
                            .warehouseId(w.getWarehouseId())
                            .warehouseName(w.getWarehouseName())
                            .quantity(w.getQuantity())
                            .lockedQuantity(w.getLockedQuantity())
                            .availableQuantity(w.getAvailableQuantity())
                            .build());
                }
            }
            if (warehouseId != null && warehouses.isEmpty()) {
                continue;
            }
            List<AiShelfStock> shelves = new ArrayList<>();
            if (row.getShelfInventoryList() != null) {
                for (InventoryPageListResp.ShelfInventory s : row.getShelfInventoryList()) {
                    shelves.add(AiShelfStock.builder()
                            .shelfId(s.getShelfId()).shelfName(s.getShelfName()).quantity(s.getQuantity())
                            .build());
                }
            }
            results.add(AiInventoryResult.builder()
                    .productId(row.getProductId())
                    .sku(row.getSku())
                    .productName(row.getProductName())
                    .spec(row.getSpec())
                    .fieldSource("CkInventoryFacade.pageList")
                    .warehouses(warehouses)
                    .shelves(shelves)
                    .remainingStockQuantityOfAllWarehouses(row.getRemainingStockQuantityOfAllWarehouses())
                    .build());
        }
        if (results.isEmpty()) {
            return ToolResult.fail(ToolErrorCode.NOT_FOUND, "未找到匹配库存");
        }
        return ToolResult.ok(Map.of("items", results, "total", results.size()), "库存查询完成");
    }
}
