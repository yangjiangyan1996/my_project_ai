package com.example.ai.tool;

import com.example.ai.context.AiExecutionContext;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
@Builder
public class ToolRequest {
    private final String toolName;
    @Builder.Default
    private final Map<String, Object> arguments = Collections.emptyMap();
    private final AiExecutionContext executionContext;
    /** Required for L3. */
    private final String confirmToken;
}
