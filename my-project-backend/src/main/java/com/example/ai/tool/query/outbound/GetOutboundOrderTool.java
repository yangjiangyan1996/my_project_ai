package com.example.ai.tool.query.outbound;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiOutboundOrderResult;
import com.example.Facade.CkOutboundFacade;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.entity.cangku.resp.OutboundDetailResp;
import com.example.entity.cangku.resp.OutboundListPageResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetOutboundOrderTool extends AbstractWmsQueryTool {

    @Resource
    private CkOutboundFacade outboundFacade;

    @Override
    public String name() {
        return "get_outbound_order";
    }

    @Override
    public String description() {
        return "按 orderId 或 orderNo 查询出库单详情。";
    }

    @Override
    public String requiredPermission() {
        return "ck:outbound:list";
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
            OutboundListPageReq req = baseReq(context);
            req.setOrderNo(orderNo);
            req.setPage(1);
            req.setSize(WmsQuerySupport.MAX_LIMIT);
            Page<OutboundListPageResp> page = outboundFacade.pageList(WmsQuerySupport.page(WmsQuerySupport.MAX_LIMIT), req);
            List<OutboundListPageResp> records = page == null ? List.of() : page.getRecords();
            if (records == null || records.isEmpty()) {
                return ToolResult.fail(ToolErrorCode.NOT_FOUND, "出库单不存在");
            }
            if (records.size() > 1) {
                List<AiOutboundOrderResult> candidates = new ArrayList<>();
                for (OutboundListPageResp r : records) {
                    candidates.add(OutboundOrderMapper.fromList(r));
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("ambiguous", true);
                data.put("items", candidates);
                data.put("total", candidates.size());
                return ToolResult.ok(data, "匹配到多个出库单，请选择");
            }
            orderId = records.get(0).getId();
        }
        OutboundDetailResp detail = outboundFacade.detail(orderId, context.getTenantId());
        return ToolResult.ok(Map.of("order", OutboundOrderMapper.fromDetail(detail)), "出库单详情");
    }

    static OutboundListPageReq baseReq(AiExecutionContext context) {
        OutboundListPageReq req = new OutboundListPageReq();
        req.setTenantId(context.getTenantId());
        req.setUserId(context.getUserId());
        return req;
    }
}
