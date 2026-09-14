package com.example.ai.tool;

import com.example.ai.exception.AiValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Guards tool-call fan-out for multi-step orchestration:
 * max calls / turn, and same-tool+args loop detection.
 */
@Component
public class ToolCallGuard {

    @Value("${ai.copilot.max-tool-calls:6}")
    private int maxToolCalls;

    @Value("${ai.copilot.max-same-tool-calls:2}")
    private int maxSameToolCalls;

    @Resource
    private ObjectMapper objectMapper;

    public int getMaxToolCalls() {
        return maxToolCalls;
    }

    public int getMaxSameToolCalls() {
        return maxSameToolCalls;
    }

    public void assertWithinLimit(int callsSoFar) {
        if (callsSoFar >= maxToolCalls) {
            throw new AiValidationException("超过单轮最大 Tool 调用次数: " + maxToolCalls);
        }
    }

    /**
     * Fingerprint = toolName + normalized arguments JSON.
     */
    public String fingerprint(String toolName, Map<String, Object> arguments) {
        String argsJson;
        try {
            Map<String, Object> sorted = new TreeMap<>();
            if (arguments != null) {
                arguments.forEach((k, v) -> {
                    if (k != null) {
                        sorted.put(k, v);
                    }
                });
            }
            argsJson = objectMapper.writeValueAsString(sorted);
        } catch (Exception e) {
            argsJson = String.valueOf(arguments);
        }
        return (toolName == null ? "" : toolName) + "|" + argsJson;
    }

    /**
     * Increments fingerprint count; throws when same tool+args exceeds threshold.
     */
    public void assertNotLooping(Map<String, AtomicInteger> fingerprintCounts, String fingerprint) {
        if (fingerprintCounts == null) {
            return;
        }
        AtomicInteger counter = fingerprintCounts.computeIfAbsent(fingerprint, k -> new AtomicInteger(0));
        int n = counter.incrementAndGet();
        if (n > maxSameToolCalls) {
            throw new AiValidationException("检测到重复 Tool 调用循环: " + fingerprint);
        }
    }

    public Map<String, AtomicInteger> newFingerprintTracker() {
        return new ConcurrentHashMap<>();
    }
}
