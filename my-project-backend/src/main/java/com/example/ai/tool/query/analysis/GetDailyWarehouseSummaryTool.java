package com.example.ai.tool.query.analysis;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.tool.ToolResult;
import com.example.ai.workspace.AiDailyWorkspaceData;
import com.example.ai.workspace.DailyWorkspaceQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetDailyWarehouseSummaryTool extends AbstractWmsAnalysisTool {

    @Resource
    private DailyWorkspaceQueryService dailyWorkspaceQueryService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String name() {
        return "get_daily_warehouse_summary";
    }

    @Override
    public String description() {
        return "获取当前租户今日仓储工作台结构化统计（待入库/待出库/待盘点/库存风险），数字来自 WMS Facade。";
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
        return schema(List.of(), Map.of());
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        AiDailyWorkspaceData data = dailyWorkspaceQueryService.build(context);
        Map<String, Object> payload = objectMapper.convertValue(data, LinkedHashMap.class);
        return ToolResult.ok(payload, "今日工作台统计");
    }
}
