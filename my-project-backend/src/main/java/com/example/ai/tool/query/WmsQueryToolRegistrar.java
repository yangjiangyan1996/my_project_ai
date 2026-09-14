package com.example.ai.tool.query;

import com.example.ai.tool.AiTool;
import com.example.ai.tool.ToolRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registers all Phase C WMS query {@link AiTool} beans into {@link ToolRegistry}.
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
        for (AiTool tool : aiTools) {
            if (toolRegistry.contains(tool.name())) {
                log.warn("Skip duplicate AI tool registration: {}", tool.name());
                continue;
            }
            toolRegistry.register(tool);
            count++;
            log.info("Registered AI WMS tool: {} risk={} permission={}",
                    tool.name(), tool.riskLevel(), tool.requiredPermission());
        }
        log.info("AI WMS Phase C production catalog size={}", toolRegistry.listAll().size());
        if (count == 0) {
            log.warn("No AiTool beans registered — Phase C catalog empty");
        }
    }
}
