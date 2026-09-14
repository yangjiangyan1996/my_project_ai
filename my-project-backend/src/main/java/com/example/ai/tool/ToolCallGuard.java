package com.example.ai.tool;

import com.example.ai.exception.AiValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Guards tool-call fan-out for future multi-step orchestration (Phase D).
 * Phase B: contract + counter only — no full agent loop.
 */
@Component
public class ToolCallGuard {

    @Value("${ai.copilot.max-tool-calls:6}")
    private int maxToolCalls;

    public int getMaxToolCalls() {
        return maxToolCalls;
    }

    public void assertWithinLimit(int callsSoFar) {
        if (callsSoFar >= maxToolCalls) {
            throw new AiValidationException("超过单轮最大 Tool 调用次数: " + maxToolCalls);
        }
    }
}
