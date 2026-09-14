package com.example.ai.config;

import com.example.ai.tool.ToolRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Logs production ToolRegistry catalog size after Phase C WMS tools register.
 */
@Slf4j
@Configuration
@DependsOn("wmsQueryToolRegistrar")
public class AiCopilotConfiguration {

    @Resource
    private ToolRegistry toolRegistry;

    @PostConstruct
    public void logProductionCatalog() {
        int size = toolRegistry.listAll().size();
        log.info("AI ToolRegistry production catalog size={} (Phase C WMS query tools)", size);
    }
}
