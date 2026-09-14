package com.example.ai.tool.query.stocktake;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiStocktakeResult;
import com.example.Facade.CkStockFacade;
import com.example.entity.cangku.req.StockListPageReq;
import com.example.entity.cangku.resp.StockDetailResp;
import com.example.entity.cangku.resp.StockListPageResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetStocktakeOrderTool extends AbstractWmsQueryTool {

    @Resource
    private CkStockFacade stockFacade;

    @Override
    public String name() {
        return "get_stocktake_order";
    }

    @Override
    public String description() {
        return "按 stockTakeId 或 orderNo 查询盘点单详情。";
    }

    @Override
    public String requiredPermission() {
        return "ck:stocktake:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("stockTakeId", Map.of("type", "integer"));
        props.put("orderNo", Map.of("type", "string"));
        return schema(List.of(), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        Long stockTakeId = WmsQuerySupport.lng(arguments, "stockTakeId");
        String orderNo = WmsQuerySupport.str(arguments, "orderNo");
        if (stockTakeId == null && orderNo == null) {
            throw new AiValidationException("需要 stockTakeId 或 orderNo");
        }
        if (stockTakeId == null) {
            StockListPageReq req = baseReq(context);
            req.setOrderNo(orderNo);
            req.setPage(1);
            req.setSize(WmsQuerySupport.MAX_LIMIT);
            Page<StockListPageResp> page = stockFacade.pageList(WmsQuerySupport.page(WmsQuerySupport.MAX_LIMIT), req);
            List<StockListPageResp> records = page == null || page.getRecords() == null
                    ? List.of()
                    : page.getRecords().stream()
                    .filter(r -> orderNo.equalsIgnoreCase(r.getStockTakeNo()))
                    .toList();
            if (records.isEmpty()) {
                return ToolResult.fail(ToolErrorCode.NOT_FOUND, "盘点单不存在");
            }
            if (records.size() > 1) {
                List<AiStocktakeResult> candidates = new ArrayList<>();
                for (StockListPageResp r : records) {
                    candidates.add(StocktakeMapper.fromList(r));
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("ambiguous", true);
                data.put("items", candidates);
                data.put("total", candidates.size());
                return ToolResult.ok(data, "匹配到多个盘点单，请选择");
            }
            stockTakeId = records.get(0).getId();
        }
        StockDetailResp detail = stockFacade.stockDetail(stockTakeId, context.getTenantId());
        if (detail == null) {
            return ToolResult.fail(ToolErrorCode.NOT_FOUND, "盘点单不存在");
        }
        return ToolResult.ok(Map.of("order", StocktakeMapper.fromDetail(detail)), "盘点单详情");
    }

    static StockListPageReq baseReq(AiExecutionContext context) {
        StockListPageReq req = new StockListPageReq();
        req.setTenantId(context.getTenantId());
        req.setUserId(context.getUserId());
        return req;
    }
}
