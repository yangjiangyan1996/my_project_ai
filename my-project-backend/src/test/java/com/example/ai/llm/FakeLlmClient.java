package com.example.ai.llm;

import com.example.ai.exception.AiModelException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test/fake LLM — never calls paid APIs.
 */
public class FakeLlmClient implements LlmClient {

    private final String fixedContent;
    private final AtomicInteger callCount = new AtomicInteger();

    public FakeLlmClient(String fixedContent) {
        this.fixedContent = fixedContent;
    }

    @Override
    public LlmChatResult chat(LlmChatRequest request) {
        if (request == null || (request.getUserMessage() == null || request.getUserMessage().isBlank())) {
            throw new AiModelException("empty");
        }
        callCount.incrementAndGet();
        return LlmChatResult.builder()
                .content(fixedContent)
                .model("fake-model")
                .latencyMs(1L)
                .inputTokens(1)
                .outputTokens(1)
                .build();
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
