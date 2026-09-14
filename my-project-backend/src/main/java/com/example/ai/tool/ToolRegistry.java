package com.example.ai.tool;

import com.example.ai.exception.AiToolException;
import com.example.ai.exception.AiValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool registry. L4 tools cannot be registered in V1.
 */
@Component
public class ToolRegistry {

    private final Map<String, ToolDefinition> tools = new LinkedHashMap<>();

    public synchronized void register(ToolDefinition definition) {
        if (definition == null || definition.getName() == null || definition.getName().isBlank()) {
            throw new AiValidationException("ToolDefinition.name 不能为空");
        }
        if (definition.getRiskLevel() == null) {
            throw new AiValidationException("ToolDefinition.riskLevel 不能为空: " + definition.getName());
        }
        if (definition.getRiskLevel().isDisabledInV1()) {
            throw new AiValidationException("V1 禁止注册 L4 高风险工具: " + definition.getName());
        }
        if (tools.containsKey(definition.getName())) {
            throw new AiValidationException("工具名称重复: " + definition.getName());
        }
        tools.put(definition.getName(), definition);
    }

    public ToolDefinition getRequired(String name) {
        ToolDefinition def = tools.get(name);
        if (def == null) {
            throw new AiToolException("未知工具: " + name);
        }
        return def;
    }

    public ToolDefinition get(String name) {
        return tools.get(name);
    }

    public boolean contains(String name) {
        return tools.containsKey(name);
    }

    public Collection<ToolDefinition> listAll() {
        return List.copyOf(tools.values());
    }

    /**
     * Metadata for LLM tool-calling (provider-agnostic maps).
     */
    public List<Map<String, Object>> toLlmToolMetadata() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ToolDefinition t : tools.values()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", t.getName());
            m.put("description", t.getDescription());
            m.put("riskLevel", t.getRiskLevel().name());
            m.put("permission", t.getPermission());
            m.put("inputSchema", t.getInputSchema());
            list.add(m);
        }
        return list;
    }

    /** Test helper / Phase reset. */
    public synchronized void clear() {
        tools.clear();
    }
}
