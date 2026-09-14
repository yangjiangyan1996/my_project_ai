package com.example.ai.tool.query.stocktake;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiStocktakeResult;
import com.example.Facade.CkStockFacade;
import com.example.entity.cangku.req.StockListPageReq;
import com.example.entity.cangku.resp.StockListPageResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CAPABILITY_GAP: CkStockTakeServiceImpl.getStockPage largely ignores filters beyond tenant.
 * Tool still passes filters; post-filters warehouseId/approvalStatus when returned.
 */
@Component
public class ListStocktakeOrdersTool extends AbstractWmsQueryTool {

    @Resource
    private CkStockFacade stockFacade;

    @Override
    public String name() {
        return "list_stocktake_orders";
    }

    @Override
    public String description() {
        return "列出当前租户盘点单。支持 warehouseId/approvalStatus/orderNo（服务层过滤能力有限，必要时客户端二次过滤）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:stocktake:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("warehouseId", Map.of("type", "integer"));
        props.put("approvalStatus", Map.of("type", "integer"));
        props.put("orderNo", Map.of("type", "string"));
        props.put("limit", Map.of("type", "integer"));
        return schema(List.of(), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        int limit = WmsQuerySupport.limit(arguments);
        Long warehouseId = WmsQuerySupport.lng(arguments, "warehouseId");
        Integer approvalStatus = WmsQuerySupport.integer(arguments, "approvalStatus");
        String orderNo = WmsQuerySupport.str(arguments, "orderNo");

        StockListPageReq req = GetStocktakeOrderTool.baseReq(context);
        req.setWarehouseId(warehouseId);
        req.setApprovalStatus(approvalStatus);
        req.setOrderNo(orderNo);
        req.setPage(1);
        req.setSize(Math.max(limit * 5, WmsQuerySupport.MAX_LIMIT));

        Page<StockListPageResp> page = stockFacade.pageList(
                WmsQuerySupport.page(req.getSize()), req);
        List<AiStocktakeResult> filtered = new ArrayList<>();
        if (page != null && page.getRecords() != null) {
            for (StockListPageResp r : page.getRecords()) {
                if (warehouseId != null && !warehouseId.equals(r.getWarehouseId())) {
                    continue;
                }
                if (approvalStatus != null && !approvalStatus.equals(r.getApprovalStatus())) {
                    continue;
                }
                if (orderNo != null && (r.getStockTakeNo() == null
                        || !r.getStockTakeNo().toLowerCase().contains(orderNo.toLowerCase()))) {
                    continue;
                }
                filtered.add(StocktakeMapper.fromList(r));
                if (filtered.size() >= limit) {
                    break;
                }
            }
        }
        long total = filtered.size();
        // CAPABILITY_GAP: underlying total may not reflect filters; report filtered page size.
        return ToolResult.ok(WmsQuerySupport.listPayload(filtered, total), "盘点单列表");
    }
}
