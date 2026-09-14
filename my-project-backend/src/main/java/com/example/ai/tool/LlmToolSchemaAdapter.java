package com.example.ai.tool;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts internal {@link ToolDefinition} to provider-agnostic LLM tool schemas.
 * Provider-specific (OpenAI/DeepSeek/Qwen) wiring stays in LLM adapter layer later.
 */
@Component
public class LlmToolSchemaAdapter {

    /**
     * Generic function-calling style schema (OpenAI-compatible shape, no SDK types).
     */
    public Map<String, Object> toFunctionSchema(ToolDefinition tool) {
        Map<String, Object> fn = new LinkedHashMap<>();
        fn.put("name", tool.getName());
        fn.put("description", tool.getDescription());
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (tool.getInputSchema() != null && !tool.getInputSchema().isEmpty()) {
            parameters.putAll(tool.getInputSchema());
            parameters.putIfAbsent("type", "object");
        } else {
            parameters.put("type", "object");
            parameters.put("properties", Map.of());
        }
        fn.put("parameters", parameters);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("type", "function");
        wrapper.put("function", fn);
        wrapper.put("riskLevel", tool.getRiskLevel().name());
        wrapper.put("permission", tool.getPermission());
        return wrapper;
    }

    public List<Map<String, Object>> toFunctionSchemas(Iterable<ToolDefinition> tools) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ToolDefinition t : tools) {
            list.add(toFunctionSchema(t));
        }
        return list;
    }
}
