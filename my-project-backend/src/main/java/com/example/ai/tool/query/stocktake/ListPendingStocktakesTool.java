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
import com.example.enums.CkStockTakeEnums;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pending = ApprovalStatus.UNDER_REVIEW (1).
 * CAPABILITY_GAP: post-filters because pageList may ignore approvalStatus.
 */
@Component
public class ListPendingStocktakesTool extends AbstractWmsQueryTool {

    @Resource
    private CkStockFacade stockFacade;

    @Override
    public String name() {
        return "list_pending_stocktakes";
    }

    @Override
    public String description() {
        return "列出当前租户待审核盘点单（审批状态=UNDER_REVIEW/审核中=1）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:stocktake:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return schema(List.of(), Map.of("limit", Map.of("type", "integer")));
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        int limit = WmsQuerySupport.limit(arguments);
        int pending = CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW.getCode();
        StockListPageReq req = GetStocktakeOrderTool.baseReq(context);
        req.setApprovalStatus(pending);
        req.setPage(1);
        req.setSize(Math.max(limit * 5, WmsQuerySupport.MAX_LIMIT));
        Page<StockListPageResp> page = stockFacade.pageList(WmsQuerySupport.page(req.getSize()), req);
        List<AiStocktakeResult> items = new ArrayList<>();
        if (page != null && page.getRecords() != null) {
            for (StockListPageResp r : page.getRecords()) {
                if (!Integer.valueOf(pending).equals(r.getApprovalStatus())) {
                    continue;
                }
                items.add(StocktakeMapper.fromList(r));
                if (items.size() >= limit) {
                    break;
                }
            }
        }
        return ToolResult.ok(WmsQuerySupport.listPayload(items, items.size()), "待审核盘点单");
    }
}
