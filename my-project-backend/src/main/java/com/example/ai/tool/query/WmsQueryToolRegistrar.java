package com.example.ai.tool.query;

import com.example.ai.tool.AiTool;
import com.example.ai.tool.ToolRegistry;
import com.example.ai.tool.ToolRiskLevel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registers LLM-callable AI tools. Skips L3/L4 — create_* must not be in LLM catalog.
 */
@Slf4j
@Component
public class WmsQueryToolRegistrar {

    @Resource
    private ToolRegistry toolRegistry;
    @Resource
    private List<AiTool> aiTools;

    @PostConstruct
    public void register() {
        int count = 0;
        int skipped = 0;
        for (AiTool tool : aiTools) {
            if (tool.riskLevel() == ToolRiskLevel.L3_CONFIRM_REQUIRED
                    || tool.riskLevel() == ToolRiskLevel.L4_HIGH_RISK
                    || tool.riskLevel().isDisabledInV1()) {
                skipped++;
                log.info("Skip LLM catalog tool (risk={}): {}", tool.riskLevel(), tool.name());
                continue;
            }
            if (toolRegistry.contains(tool.name())) {
                log.warn("Skip duplicate AI tool registration: {}", tool.name());
                continue;
            }
            toolRegistry.register(tool);
            count++;
            log.info("Registered AI WMS tool: {} risk={} permission={}",
                    tool.name(), tool.riskLevel(), tool.requiredPermission());
        }
        log.info("AI ToolRegistry catalog size={} registered={} skippedL3orL4={}",
                toolRegistry.listAll().size(), count, skipped);
        if (count == 0) {
            log.warn("No AiTool beans registered — catalog empty");
        }
    }
}
