package com.example.ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compresses ToolResult payloads before they enter LLM conversation context.
 */
@Component
public class ToolResultContextCompressor {

    public static final int MAX_LIST_ITEMS = 20;
    public static final int MAX_JSON_CHARS = 4000;

    @Resource
    private ObjectMapper objectMapper;

    public String compressForLlm(ToolResult result) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("success", result != null && result.isSuccess());
        envelope.put("errorCode", result == null ? "SYSTEM_ERROR" : result.getErrorCode());
        envelope.put("message", result == null ? "空结果" : result.getMessage());
        if (result != null && result.getData() != null) {
            envelope.put("data", prune(result.getData(), 0));
        }
        try {
            String json = objectMapper.writeValueAsString(envelope);
            if (json.length() > MAX_JSON_CHARS) {
                return json.substring(0, MAX_JSON_CHARS) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return "{\"success\":false,\"errorCode\":\"SYSTEM_ERROR\",\"message\":\"结果序列化失败\"}";
        }
    }

    @SuppressWarnings("unchecked")
    private Object prune(Object value, int depth) {
        if (value == null || depth > 6) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                String key = String.valueOf(e.getKey());
                if (isSensitiveKey(key)) {
                    continue;
                }
                Object pruned = prune(e.getValue(), depth + 1);
                if (pruned != null) {
                    out.put(key, pruned);
                }
            }
            return out;
        }
        if (value instanceof List<?> list) {
            List<Object> out = new ArrayList<>();
            int limit = Math.min(list.size(), MAX_LIST_ITEMS);
            for (int i = 0; i < limit; i++) {
                Object pruned = prune(list.get(i), depth + 1);
                if (pruned != null) {
                    out.add(pruned);
                }
            }
            if (list.size() > MAX_LIST_ITEMS) {
                out.add(Map.of("_truncated", true, "_omitted", list.size() - MAX_LIST_ITEMS));
            }
            return out;
        }
        if (value instanceof String s && s.length() > 500) {
            return s.substring(0, 500) + "...";
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        String k = key.toLowerCase();
        return k.contains("tenant")
                || k.contains("password")
                || k.contains("secret")
                || k.contains("token")
                || k.equals("jwt")
                || k.contains("authorization");
    }
}
