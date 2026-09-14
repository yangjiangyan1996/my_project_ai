package com.example.ai.draft;

import com.example.Facade.CkInboundFacade;
import com.example.Facade.CkOutboundFacade;
import com.example.Facade.CkStockFacade;
import com.example.ai.context.AiExecutionContext;
import com.example.entity.cangku.req.InboundCreateReq;
import com.example.entity.cangku.req.OutboundCreateSaleProductReq;
import com.example.entity.cangku.req.StockTakeCreateReq;
import com.example.service.CkStockTakeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * L3 create path: reloads Facade-shaped payload from Draft and calls existing Facades only.
 * Never exposed to LLM ToolRegistry catalog.
 */
@Component
public class AiDraftOrderCreator {

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private CkInboundFacade inboundFacade;
    @Resource
    private CkOutboundFacade outboundFacade;
    @Resource
    private CkStockFacade stockFacade;
    @Resource
    private CkStockTakeService stockTakeService;

    public CreateOutcome create(AiExecutionContext ctx, AiDraft draft) {
        return switch (draft.getDraftType()) {
            case INBOUND -> createInbound(ctx, draft);
            case OUTBOUND -> createOutbound(ctx, draft);
            case STOCKTAKE -> createStocktake(ctx, draft);
        };
    }

    private CreateOutcome createInbound(AiExecutionContext ctx, AiDraft draft) {
        InboundCreateReq req = objectMapper.convertValue(draft.getPayload(), InboundCreateReq.class);
        req.setTenantId(ctx.getTenantId());
        req.setUserId(ctx.getUserId());
        // status: keep traditional draft state from payload (typically WaitSubmit=0); never auto-approve
        Boolean ok = inboundFacade.create(req);
        if (!Boolean.TRUE.equals(ok)) {
            throw new IllegalStateException("入库单创建返回失败");
        }
        return new CreateOutcome(null, req.getOrderNo());
    }

    private CreateOutcome createOutbound(AiExecutionContext ctx, AiDraft draft) {
        OutboundCreateSaleProductReq req =
                objectMapper.convertValue(draft.getPayload(), OutboundCreateSaleProductReq.class);
        req.setTenantId(ctx.getTenantId());
        req.setUserId(ctx.getUserId());
        Boolean ok = outboundFacade.createProductionSaleOutBound(req);
        if (!Boolean.TRUE.equals(ok)) {
            throw new IllegalStateException("出库单创建返回失败");
        }
        return new CreateOutcome(null, req.getOrderNo());
    }

    private CreateOutcome createStocktake(AiExecutionContext ctx, AiDraft draft) {
        StockTakeCreateReq req = objectMapper.convertValue(draft.getPayload(), StockTakeCreateReq.class);
        req.setTenantId(ctx.getTenantId());
        req.setUserId(ctx.getUserId());
        Long id = stockFacade.createStockTake(req);
        String no = null;
        try {
            var st = stockTakeService.getById(id);
            if (st != null) {
                no = st.getStockTakeNo();
            }
        } catch (Exception ignored) {
            // orderId is enough for VIEW action
        }
        return new CreateOutcome(id, no == null ? String.valueOf(id) : no);
    }

    public record CreateOutcome(Long orderId, String orderNo) {
    }
}
