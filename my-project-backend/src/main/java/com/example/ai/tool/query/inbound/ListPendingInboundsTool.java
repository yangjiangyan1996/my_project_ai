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
import com.example.enums.CkInOutboundEnums;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pending = InOutBoundStatus.WaitAudit (1) — 审核中.
 */
@Component
public class ListPendingInboundsTool extends AbstractWmsQueryTool {

    @Resource
    private CkInboundFacade inboundFacade;

    @Override
    public String name() {
        return "list_pending_inbounds";
    }

    @Override
    public String description() {
        return "列出当前租户待审核入库单（状态=WaitAudit/审核中=1）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:inbound:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return schema(List.of(), Map.of("limit", Map.of("type", "integer")));
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        int limit = WmsQuerySupport.limit(arguments);
        InboundListPageReq req = GetInboundOrderTool.baseReq(context);
        req.setStatus(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode());
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
        return ToolResult.ok(WmsQuerySupport.listPayload(items, total), "待审核入库单");
    }
}
