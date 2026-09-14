package com.example.ai.config;

import com.example.ai.tool.ToolRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Production catalog stays empty until Phase C. Fake tools must live in test sources only.
 */
@Slf4j
@Configuration
public class AiCopilotConfiguration {

    @Resource
    private ToolRegistry toolRegistry;

    @PostConstruct
    public void assertEmptyProductionCatalog() {
        int size = toolRegistry.listAll().size();
        if (size > 0) {
            log.warn("AI ToolRegistry has {} tools at startup — ensure no Fake tools in production", size);
        } else {
            log.info("AI ToolRegistry production catalog empty (Phase B). Phase C will register WMS tools.");
        }
    }
}
