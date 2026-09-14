package com.example.ai.llm;

import com.example.ai.exception.AiModelException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Test/fake LLM — never calls paid APIs. Supports fixed text or scripted tool-calling turns.
 */
public class FakeLlmClient implements LlmClient {

    private final AtomicInteger callCount = new AtomicInteger();
    private final List<Function<LlmChatRequest, LlmChatResult>> script = new ArrayList<>();
    private String fixedContent;

    public FakeLlmClient(String fixedContent) {
        this.fixedContent = fixedContent;
    }

    public FakeLlmClient(List<Function<LlmChatRequest, LlmChatResult>> scripted) {
        if (scripted != null) {
            this.script.addAll(scripted);
        }
    }

    @SafeVarargs
    public static FakeLlmClient scripted(Function<LlmChatRequest, LlmChatResult>... turns) {
        return new FakeLlmClient(List.of(turns));
    }

    public static LlmChatResult text(String content) {
        return LlmChatResult.builder()
                .content(content)
                .finishReason("stop")
                .model("fake-model")
                .latencyMs(1L)
                .inputTokens(2)
                .outputTokens(3)
                .build();
    }

    public static LlmChatResult toolCall(String id, String name, Map<String, Object> args) {
        return LlmChatResult.builder()
                .content("")
                .finishReason("tool_calls")
                .model("fake-model")
                .latencyMs(1L)
                .inputTokens(2)
                .outputTokens(3)
                .toolCalls(List.of(LlmToolCall.builder()
                        .id(id)
                        .name(name)
                        .arguments(args == null ? Map.of() : args)
                        .argumentsJson(null)
                        .build()))
                .build();
    }

    @Override
    public LlmChatResult chat(LlmChatRequest request) {
        if (request == null || !hasUserFacingContent(request)) {
            throw new AiModelException("empty");
        }
        int n = callCount.getAndIncrement();
        if (!script.isEmpty()) {
            if (n >= script.size()) {
                return text("(script exhausted)");
            }
            return script.get(n).apply(request);
        }
        return text(fixedContent == null ? "" : fixedContent);
    }

    private boolean hasUserFacingContent(LlmChatRequest request) {
        if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
            return true;
        }
        if (request.getMessages() != null) {
            for (LlmMessage m : request.getMessages()) {
                if (m != null && m.getRole() != null
                        && ("user".equalsIgnoreCase(m.getRole())
                        || "tool".equalsIgnoreCase(m.getRole())
                        || "assistant".equalsIgnoreCase(m.getRole()))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String providerId() {
        return "fake";
    }

    @Override
    public String defaultModel() {
        return "fake-model";
    }

    public int getCallCount() {
        return callCount.get();
    }
}
