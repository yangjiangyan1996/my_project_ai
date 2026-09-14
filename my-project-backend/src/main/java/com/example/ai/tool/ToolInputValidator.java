package com.example.ai.tool;

import com.example.ai.exception.AiValidationException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON-schema subset validation before tool execute.
 * Supports: required[], properties.{name}.type in {string,number,integer,boolean,object,array}
 */
@Component
public class ToolInputValidator {

    @SuppressWarnings("unchecked")
    public void validate(Map<String, Object> inputSchema, Map<String, Object> arguments) {
        Map<String, Object> args = arguments == null ? Collections.emptyMap() : arguments;
        Map<String, Object> schema = inputSchema == null ? Collections.emptyMap() : inputSchema;
        if (schema.isEmpty()) {
            return;
        }

        Object requiredObj = schema.get("required");
        if (requiredObj instanceof Collection<?> required) {
            for (Object key : required) {
                if (key == null) {
                    continue;
                }
                String field = String.valueOf(key);
                if (!args.containsKey(field) || args.get(field) == null) {
                    throw new AiValidationException("缺少必填参数: " + field);
                }
            }
        }

        Object propsObj = schema.get("properties");
        if (!(propsObj instanceof Map<?, ?> props)) {
            return;
        }
        for (Map.Entry<?, ?> e : props.entrySet()) {
            String field = String.valueOf(e.getKey());
            if (!args.containsKey(field) || args.get(field) == null) {
                continue;
            }
            if (!(e.getValue() instanceof Map<?, ?> propSchema)) {
                continue;
            }
            Object type = propSchema.get("type");
            if (type == null) {
                continue;
            }
            assertType(field, String.valueOf(type), args.get(field));
        }
    }

    private void assertType(String field, String type, Object value) {
        boolean ok = switch (type) {
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            case "integer" -> value instanceof Integer || value instanceof Long
                    || (value instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue()));
            case "boolean" -> value instanceof Boolean;
            case "object" -> value instanceof Map<?, ?>;
            case "array" -> value instanceof List<?> || value instanceof Object[];
            default -> true;
        };
        if (!ok) {
            throw new AiValidationException("参数类型错误: " + field + " 期望 " + type);
        }
    }
}
