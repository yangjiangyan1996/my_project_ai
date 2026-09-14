package com.example.ai.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured Daily Workspace payload — numbers from WMS Facades only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDailyWorkspaceData {

    private String date;

    @Builder.Default
    private InboundBlock inbound = new InboundBlock();
    @Builder.Default
    private OutboundBlock outbound = new OutboundBlock();
    @Builder.Default
    private StocktakeBlock stocktake = new StocktakeBlock();
    @Builder.Default
    private InventoryBlock inventory = new InventoryBlock();

    @Builder.Default
    private List<PriorityItem> priorityItems = new ArrayList<>();

    @Builder.Default
    private List<String> dataWarnings = new ArrayList<>();

    /** Sum of actionable pending items for greeting. */
    private long totalActionable;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InboundBlock {
        private long pendingCount;
        private long todayCount;
        /** WaitAudit=1 — aligned with list_pending_inbounds */
        private String pendingStatusRule;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutboundBlock {
        private long pendingCount;
        private long todayCount;
        private String pendingStatusRule;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StocktakeBlock {
        private long pendingCount;
        private String pendingStatusRule;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryBlock {
        /** Rule-based: product minStock vs inventory quantity (Facade countsOfIndexPage.lowStock). */
        private long riskCount;
        private String riskRule;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityItem {
        private String kind;
        private String title;
        private String detail;
        private String actionType;
        private String entityId;
    }
}
