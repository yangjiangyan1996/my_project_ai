package com.example.ai.tool.query.analysis;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.tool.ToolResult;
import com.example.ai.workspace.AiDailyWorkspaceData;
import com.example.ai.workspace.DailyWorkspaceQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CAPABILITY_GAP: WMS has no reliable per-assignee pending queue.
 * Returns tenant-visible pending items (same scope as Daily Workspace priority list).
 */
@Component
public class GetMyPendingTasksTool extends AbstractWmsAnalysisTool {

    @Resource
    private DailyWorkspaceQueryService dailyWorkspaceQueryService;

    @Override
    public String name() {
        return "get_my_pending_tasks";
    }

    @Override
    public String description() {
        return "获取当前租户可见的待处理任务列表（入库/出库/盘点/库存风险样本）。非个人派工队列。";
    }

    @Override
    public String requiredPermission() {
        return "ck:dashboard:read";
    }

    @Override
    public long timeoutMs() {
        return 8000L;
    }

    @Override
    public Map<String, Object> inputSchema() {
        return schema(List.of(), Map.of("limit", Map.of("type", "integer")));
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        AiDailyWorkspaceData data = dailyWorkspaceQueryService.build(context);
        int limit = 20;
        Object raw = arguments.get("limit");
        if (raw instanceof Number n) {
            limit = Math.min(50, Math.max(1, n.intValue()));
        }
        List<AiDailyWorkspaceData.PriorityItem> items = data.getPriorityItems() == null
                ? List.of()
                : data.getPriorityItems();
        if (items.size() > limit) {
            items = items.subList(0, limit);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", items);
        payload.put("total", items.size());
        payload.put("capabilityGap", "NOT_USER_ASSIGNED_QUEUE");
        payload.put("scope", "tenant_visible_pending");
        payload.put("inboundPending", data.getInbound().getPendingCount());
        payload.put("outboundPending", data.getOutbound().getPendingCount());
        payload.put("stocktakePending", data.getStocktake().getPendingCount());
        payload.put("inventoryRisk", data.getInventory().getRiskCount());
        return ToolResult.ok(payload, "待办任务（租户可见范围）");
    }
}
