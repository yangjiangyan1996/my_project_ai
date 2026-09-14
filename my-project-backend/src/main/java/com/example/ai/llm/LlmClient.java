package com.example.ai.llm;

/**
 * Abstraction over LLM providers. Business code must depend on this, not DeepSeekUtils.
 */
public interface LlmClient {

    LlmChatResult chat(LlmChatRequest request);

    /** Provider id e.g. deepseek / qwen / openai */
    String providerId();

    /** Default model name for audit. */
    String defaultModel();
}
