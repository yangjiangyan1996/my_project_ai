package com.example.ai.tool;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
@Builder
public class ToolResult {
    private boolean success;
    private String errorCode;
    private String message;
    @Builder.Default
    private Map<String, Object> data = Collections.emptyMap();

    public static ToolResult ok(Map<String, Object> data, String message) {
        return ToolResult.builder().success(true).data(data).message(message).build();
    }

    public static ToolResult fail(String errorCode, String message) {
        return ToolResult.builder().success(false).errorCode(errorCode).message(message).build();
    }
}
