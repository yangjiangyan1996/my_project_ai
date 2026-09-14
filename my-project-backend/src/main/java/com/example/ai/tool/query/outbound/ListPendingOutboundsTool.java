package com.example.ai.tool.query.outbound;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiOutboundOrderResult;
import com.example.Facade.CkOutboundFacade;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.entity.cangku.resp.OutboundListPageResp;
import com.example.enums.CkInOutboundEnums;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Pending = WaitAudit (1). */
@Component
public class ListPendingOutboundsTool extends AbstractWmsQueryTool {

    @Resource
    private CkOutboundFacade outboundFacade;

    @Override
    public String name() {
        return "list_pending_outbounds";
    }

    @Override
    public String description() {
        return "列出当前租户待审核出库单（状态=WaitAudit/审核中=1）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:outbound:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return schema(List.of(), Map.of("limit", Map.of("type", "integer")));
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        int limit = WmsQuerySupport.limit(arguments);
        OutboundListPageReq req = GetOutboundOrderTool.baseReq(context);
        req.setStatus(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode());
        req.setPage(1);
        req.setSize(limit);
        Page<OutboundListPageResp> page = outboundFacade.pageList(WmsQuerySupport.page(limit), req);
        List<AiOutboundOrderResult> items = new ArrayList<>();
        long total = 0;
        if (page != null) {
            total = page.getTotal();
            if (page.getRecords() != null) {
                for (OutboundListPageResp r : page.getRecords()) {
                    items.add(OutboundOrderMapper.fromList(r));
                }
            }
        }
        return ToolResult.ok(WmsQuerySupport.listPayload(items, total), "待审核出库单");
    }
}
