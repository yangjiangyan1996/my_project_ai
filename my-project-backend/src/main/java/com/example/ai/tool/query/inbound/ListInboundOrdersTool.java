package com.example.ai.tool.query.inbound;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiInboundOrderResult;
import com.example.Facade.CkInboundFacade;
import com.example.entity.cangku.req.InboundListPageReq;
import com.example.entity.cangku.resp.InboundListPageResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ListInboundOrdersTool extends AbstractWmsQueryTool {

    @Resource
    private CkInboundFacade inboundFacade;

    @Override
    public String name() {
        return "list_inbound_orders";
    }

    @Override
    public String description() {
        return "分页列出当前租户入库单。支持 status/warehouseId/orderNo/startDate/endDate。";
    }

    @Override
    public String requiredPermission() {
        return "ck:inbound:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("status", Map.of("type", "integer"));
        props.put("warehouseId", Map.of("type", "integer"));
        props.put("orderNo", Map.of("type", "string"));
        props.put("startDate", Map.of("type", "string"));
        props.put("endDate", Map.of("type", "string"));
        props.put("limit", Map.of("type", "integer"));
        return schema(List.of(), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        int limit = WmsQuerySupport.limit(arguments);
        InboundListPageReq req = GetInboundOrderTool.baseReq(context);
        req.setStatus(WmsQuerySupport.integer(arguments, "status"));
        req.setWarehouseId(WmsQuerySupport.lng(arguments, "warehouseId"));
        req.setOrderNo(WmsQuerySupport.str(arguments, "orderNo"));
        req.setStartDate(WmsQuerySupport.str(arguments, "startDate"));
        req.setEndDate(WmsQuerySupport.str(arguments, "endDate"));
        req.setPage(1);
        req.setSize(limit);
        Page<InboundListPageResp> page = inboundFacade.pageList(WmsQuerySupport.page(limit), req);
        List<AiInboundOrderResult> items = new ArrayList<>();
        long total = 0;
        if (page != null) {
            total = page.getTotal();
            if (page.getRecords() != null) {
                for (InboundListPageResp r : page.getRecords()) {
                    items.add(InboundOrderMapper.fromList(r));
                }
            }
        }
        return ToolResult.ok(WmsQuerySupport.listPayload(items, total), "入库单列表");
    }
}
