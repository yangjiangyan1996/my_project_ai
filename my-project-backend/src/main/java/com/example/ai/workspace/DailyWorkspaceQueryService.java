package com.example.ai.workspace;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.Facade.CkInboundFacade;
import com.example.Facade.CkInventoryFacade;
import com.example.Facade.CkOutboundFacade;
import com.example.Facade.CkStockFacade;
import com.example.entity.cangku.req.InboundListPageReq;
import com.example.entity.cangku.req.InventoryAlertReq;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.entity.cangku.req.StockListPageReq;
import com.example.entity.cangku.resp.InboundListPageResp;
import com.example.entity.cangku.resp.InventoryAlertResp;
import com.example.entity.cangku.resp.InventoryCountsOfIndexPageResp;
import com.example.entity.cangku.resp.OutboundListPageResp;
import com.example.entity.cangku.resp.StockListPageResp;
import com.example.enums.CkInOutboundEnums;
import com.example.enums.CkStockTakeEnums;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Composes Daily Workspace facts from existing Facades only — no Mapper / SQL.
 */
@Slf4j
@Service
public class DailyWorkspaceQueryService {

    private static final int PRIORITY_CAP = 8;

    @Resource
    private CkInventoryFacade inventoryFacade;
    @Resource
    private CkInboundFacade inboundFacade;
    @Resource
    private CkOutboundFacade outboundFacade;
    @Resource
    private CkStockFacade stockFacade;

    public AiDailyWorkspaceData build(AiExecutionContext ctx) {
        List<String> warnings = new ArrayList<>();
        String today = LocalDate.now().toString();

        InventoryCountsOfIndexPageResp index = safeIndex(ctx.getTenantId(), warnings);

        long todayIn = index == null || index.getTodayInbound() == null ? 0L : index.getTodayInbound();
        long todayOut = index == null || index.getTodayOutbound() == null ? 0L : index.getTodayOutbound();
        long lowStock = index == null || index.getLowStock() == null ? 0L : index.getLowStock();

        long pendingIn = countPendingInbound(ctx, warnings);
        long pendingOut = countPendingOutbound(ctx, warnings);
        long pendingStock = countPendingStocktake(ctx, warnings);

        List<AiDailyWorkspaceData.PriorityItem> priority = new ArrayList<>();
        appendPendingInboundSamples(ctx, priority, warnings);
        appendPendingOutboundSamples(ctx, priority, warnings);
        appendAlertSamples(ctx, priority, warnings);
        appendPendingStockSamples(ctx, priority, warnings);
        if (priority.size() > PRIORITY_CAP) {
            priority = new ArrayList<>(priority.subList(0, PRIORITY_CAP));
        }

        long total = pendingIn + pendingOut + pendingStock + lowStock;

        // Index todo* fields use WaitSubmit(0); we use WaitAudit(1) — document gap.
        if (index != null) {
            Long todoIn = index.getTodoInBoundApproval();
            Long todoOut = index.getTodoOutBoundApproval();
            if (todoIn != null && todoIn != pendingIn || todoOut != null && todoOut != pendingOut) {
                warnings.add("INDEX_TODO_USES_WAIT_SUBMIT_WHILE_PENDING_USES_WAIT_AUDIT");
            }
        }

        return AiDailyWorkspaceData.builder()
                .date(today)
                .inbound(AiDailyWorkspaceData.InboundBlock.builder()
                        .pendingCount(pendingIn)
                        .todayCount(todayIn)
                        .pendingStatusRule("WaitAudit(1)")
                        .build())
                .outbound(AiDailyWorkspaceData.OutboundBlock.builder()
                        .pendingCount(pendingOut)
                        .todayCount(todayOut)
                        .pendingStatusRule("WaitAudit(1)")
                        .build())
                .stocktake(AiDailyWorkspaceData.StocktakeBlock.builder()
                        .pendingCount(pendingStock)
                        .pendingStatusRule("ApprovalStatus.UNDER_REVIEW(1)")
                        .build())
                .inventory(AiDailyWorkspaceData.InventoryBlock.builder()
                        .riskCount(lowStock)
                        .riskRule("quantity<=product.minStock via CkInventoryFacade.countsOfIndexPage.lowStock")
                        .build())
                .priorityItems(priority)
                .dataWarnings(warnings)
                .totalActionable(total)
                .build();
    }

    private InventoryCountsOfIndexPageResp safeIndex(Long tenantId, List<String> warnings) {
        try {
            return inventoryFacade.countsOfIndexPage(tenantId);
        } catch (Exception e) {
            log.warn("countsOfIndexPage failed: {}", e.getMessage());
            warnings.add("INDEX_COUNTS_UNAVAILABLE");
            return null;
        }
    }

    private long countPendingInbound(AiExecutionContext ctx, List<String> warnings) {
        try {
            InboundListPageReq req = new InboundListPageReq();
            req.setTenantId(ctx.getTenantId());
            req.setUserId(ctx.getUserId());
            req.setStatus(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode());
            req.setPage(1);
            req.setSize(1);
            Page<InboundListPageResp> page = inboundFacade.pageList(WmsQuerySupport.page(1), req);
            return page == null ? 0L : page.getTotal();
        } catch (Exception e) {
            warnings.add("INBOUND_PENDING_UNAVAILABLE");
            return 0L;
        }
    }

    private long countPendingOutbound(AiExecutionContext ctx, List<String> warnings) {
        try {
            OutboundListPageReq req = new OutboundListPageReq();
            req.setTenantId(ctx.getTenantId());
            req.setUserId(ctx.getUserId());
            req.setStatus(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode());
            req.setPage(1);
            req.setSize(1);
            Page<OutboundListPageResp> page = outboundFacade.pageList(WmsQuerySupport.page(1), req);
            return page == null ? 0L : page.getTotal();
        } catch (Exception e) {
            warnings.add("OUTBOUND_PENDING_UNAVAILABLE");
            return 0L;
        }
    }

    private long countPendingStocktake(AiExecutionContext ctx, List<String> warnings) {
        try {
            int pending = CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW.getCode();
            StockListPageReq req = new StockListPageReq();
            req.setTenantId(ctx.getTenantId());
            req.setUserId(ctx.getUserId());
            req.setApprovalStatus(pending);
            req.setPage(1);
            req.setSize(WmsQuerySupport.MAX_LIMIT);
            Page<StockListPageResp> page = stockFacade.pageList(WmsQuerySupport.page(req.getSize()), req);
            if (page == null || page.getRecords() == null) {
                return 0L;
            }
            // CAPABILITY_GAP: pageList may ignore filters — post-filter
            return page.getRecords().stream()
                    .filter(r -> Integer.valueOf(pending).equals(r.getApprovalStatus()))
                    .count();
        } catch (Exception e) {
            warnings.add("STOCKTAKE_PENDING_UNAVAILABLE");
            return 0L;
        }
    }

    private void appendPendingInboundSamples(AiExecutionContext ctx,
                                             List<AiDailyWorkspaceData.PriorityItem> priority,
                                             List<String> warnings) {
        try {
            InboundListPageReq req = new InboundListPageReq();
            req.setTenantId(ctx.getTenantId());
            req.setUserId(ctx.getUserId());
            req.setStatus(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode());
            req.setPage(1);
            req.setSize(3);
            Page<InboundListPageResp> page = inboundFacade.pageList(WmsQuerySupport.page(3), req);
            if (page == null || page.getRecords() == null) {
                return;
            }
            for (InboundListPageResp r : page.getRecords()) {
                priority.add(AiDailyWorkspaceData.PriorityItem.builder()
                        .kind("INBOUND")
                        .title(r.getOrderNo() == null ? "入库单" : r.getOrderNo())
                        .detail("待审核入库 · " + nullToDash(r.getWarehouseName()))
                        .actionType("VIEW_INBOUND")
                        .entityId(r.getId() == null ? null : String.valueOf(r.getId()))
                        .build());
            }
        } catch (Exception e) {
            warnings.add("INBOUND_SAMPLES_UNAVAILABLE");
        }
    }

    private void appendPendingOutboundSamples(AiExecutionContext ctx,
                                              List<AiDailyWorkspaceData.PriorityItem> priority,
                                              List<String> warnings) {
        try {
            OutboundListPageReq req = new OutboundListPageReq();
            req.setTenantId(ctx.getTenantId());
            req.setUserId(ctx.getUserId());
            req.setStatus(CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode());
            req.setPage(1);
            req.setSize(3);
            Page<OutboundListPageResp> page = outboundFacade.pageList(WmsQuerySupport.page(3), req);
            if (page == null || page.getRecords() == null) {
                return;
            }
            for (OutboundListPageResp r : page.getRecords()) {
                priority.add(AiDailyWorkspaceData.PriorityItem.builder()
                        .kind("OUTBOUND")
                        .title(r.getOrderNo() == null ? "出库单" : r.getOrderNo())
                        .detail("待审核出库 · " + nullToDash(r.getWarehouseName()))
                        .actionType("VIEW_OUTBOUND")
                        .entityId(r.getId() == null ? null : String.valueOf(r.getId()))
                        .build());
            }
        } catch (Exception e) {
            warnings.add("OUTBOUND_SAMPLES_UNAVAILABLE");
        }
    }

    private void appendPendingStockSamples(AiExecutionContext ctx,
                                           List<AiDailyWorkspaceData.PriorityItem> priority,
                                           List<String> warnings) {
        try {
            int pending = CkStockTakeEnums.ApprovalStatus.UNDER_REVIEW.getCode();
            StockListPageReq req = new StockListPageReq();
            req.setTenantId(ctx.getTenantId());
            req.setUserId(ctx.getUserId());
            req.setPage(1);
            req.setSize(20);
            Page<StockListPageResp> page = stockFacade.pageList(WmsQuerySupport.page(20), req);
            if (page == null || page.getRecords() == null) {
                return;
            }
            int added = 0;
            for (StockListPageResp r : page.getRecords()) {
                if (!Integer.valueOf(pending).equals(r.getApprovalStatus())) {
                    continue;
                }
                priority.add(AiDailyWorkspaceData.PriorityItem.builder()
                        .kind("STOCKTAKE")
                        .title(r.getStockTakeNo() == null ? "盘点单" : r.getStockTakeNo())
                        .detail("待审核盘点 · " + nullToDash(r.getWarehouseName()))
                        .actionType("VIEW_STOCKTAKE")
                        .entityId(r.getId() == null ? null : String.valueOf(r.getId()))
                        .build());
                if (++added >= 2) {
                    break;
                }
            }
        } catch (Exception e) {
            warnings.add("STOCKTAKE_SAMPLES_UNAVAILABLE");
        }
    }

    private void appendAlertSamples(AiExecutionContext ctx,
                                    List<AiDailyWorkspaceData.PriorityItem> priority,
                                    List<String> warnings) {
        try {
            InventoryAlertReq req = new InventoryAlertReq();
            req.setTenantId(ctx.getTenantId());
            req.setUserId(ctx.getUserId());
            req.setPage(1);
            req.setSize(5);
            List<InventoryAlertResp> alerts = inventoryFacade.getInventoryAlerts(req);
            if (alerts == null) {
                return;
            }
            int added = 0;
            for (InventoryAlertResp a : alerts) {
                if (a.getAlertLevel() == null || "normal".equalsIgnoreCase(a.getAlertLevel())) {
                    continue;
                }
                priority.add(AiDailyWorkspaceData.PriorityItem.builder()
                        .kind("INVENTORY_RISK")
                        .title(a.getSku() == null ? a.getProductName() : a.getSku())
                        .detail("库存风险 · " + a.getAlertLevel()
                                + " · 当前=" + a.getCurrentStock()
                                + " · 仓=" + nullToDash(a.getWarehouseName()))
                        .actionType("VIEW_INVENTORY")
                        .entityId(a.getProductId() == null ? null : String.valueOf(a.getProductId()))
                        .build());
                if (++added >= 3) {
                    break;
                }
            }
        } catch (Exception e) {
            warnings.add("INVENTORY_ALERTS_UNAVAILABLE");
        }
    }

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
