package com.example.ai.tool.query.inbound;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiInboundOrderResult;
import com.example.ai.tool.query.dto.AiQueryDtos.AiOrderLine;
import com.example.Facade.CkInboundFacade;
import com.example.entity.cangku.req.InboundListPageReq;
import com.example.entity.cangku.resp.InboundDetailResp;
import com.example.entity.cangku.resp.InboundListPageResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetInboundOrderTool extends AbstractWmsQueryTool {

    @Resource
    private CkInboundFacade inboundFacade;

    @Override
    public String name() {
        return "get_inbound_order";
    }

    @Override
    public String description() {
        return "按 orderId 或 orderNo 查询入库单详情。";
    }

    @Override
    public String requiredPermission() {
        return "ck:inbound:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("orderId", Map.of("type", "integer"));
        props.put("orderNo", Map.of("type", "string"));
        return schema(List.of(), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        Long orderId = WmsQuerySupport.lng(arguments, "orderId");
        String orderNo = WmsQuerySupport.str(arguments, "orderNo");
        if (orderId == null && orderNo == null) {
            throw new AiValidationException("需要 orderId 或 orderNo");
        }
        if (orderId == null) {
            InboundListPageReq req = baseReq(context);
            req.setOrderNo(orderNo);
            req.setPage(1);
            req.setSize(WmsQuerySupport.MAX_LIMIT);
            Page<InboundListPageResp> page = inboundFacade.pageList(WmsQuerySupport.page(WmsQuerySupport.MAX_LIMIT), req);
            List<InboundListPageResp> records = page == null ? List.of() : page.getRecords();
            if (records == null || records.isEmpty()) {
                return ToolResult.fail(ToolErrorCode.NOT_FOUND, "入库单不存在");
            }
            if (records.size() > 1) {
                List<AiInboundOrderResult> candidates = new ArrayList<>();
                for (InboundListPageResp r : records) {
                    candidates.add(InboundOrderMapper.fromList(r));
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("ambiguous", true);
                data.put("items", candidates);
                data.put("total", candidates.size());
                return ToolResult.ok(data, "匹配到多个入库单，请选择");
            }
            orderId = records.get(0).getId();
        }
        InboundDetailResp detail = inboundFacade.detail(orderId, context.getTenantId());
        return ToolResult.ok(Map.of("order", InboundOrderMapper.fromDetail(detail)), "入库单详情");
    }

    static InboundListPageReq baseReq(AiExecutionContext context) {
        InboundListPageReq req = new InboundListPageReq();
        req.setTenantId(context.getTenantId());
        req.setUserId(context.getUserId());
        return req;
    }
}
