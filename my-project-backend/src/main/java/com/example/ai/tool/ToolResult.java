package com.example.ai.tool;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
@Builder
public class ToolResult {
    private boolean success;
    /** Prefer {@link ToolErrorCode#name()}; kept as String for flexibility. */
    private String errorCode;
    private String message;
    @Builder.Default
    private Map<String, Object> data = Collections.emptyMap();
    @Builder.Default
    private Map<String, Object> metadata = Collections.emptyMap();

    public static ToolResult ok(Map<String, Object> data, String message) {
        return ToolResult.builder()
                .success(true)
                .errorCode(ToolErrorCode.SUCCESS.name())
                .data(data == null ? Collections.emptyMap() : data)
                .message(message)
                .build();
    }

    public static ToolResult fail(ToolErrorCode code, String message) {
        return ToolResult.builder()
                .success(false)
                .errorCode(code == null ? ToolErrorCode.SYSTEM_ERROR.name() : code.name())
                .message(message)
                .build();
    }

    public static ToolResult fail(String errorCode, String message) {
        return ToolResult.builder().success(false).errorCode(errorCode).message(message).build();
    }
}
