package com.example.ai.llm;

import com.example.ai.exception.AiModelException;
import com.example.utils.DeepSeekUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DeepSeek adapter. Wraps existing {@link DeepSeekUtils}; does not duplicate HTTP client.
 */
@Component
public class DeepSeekLlmClient implements LlmClient {

    @Resource
    private DeepSeekUtils deepSeekUtils;

    @Value("${deepseek.api.model:deepseek-chat}")
    private String model;

    @Override
    public LlmChatResult chat(LlmChatRequest request) {
        if (request == null) {
            throw new AiModelException("LLM 请求不能为空");
        }
        String userMessage = resolveUserMessage(request);
        if (userMessage == null || userMessage.isBlank()) {
            throw new AiModelException("LLM user message 不能为空");
        }
        long start = System.currentTimeMillis();
        try {
            String content = deepSeekUtils.callDeepSeek(userMessage, request.getSystemPrompt());
            return LlmChatResult.builder()
                    .content(content)
                    .model(model)
                    .latencyMs(System.currentTimeMillis() - start)
                    .build();
        } catch (AiModelException e) {
            throw e;
        } catch (Exception e) {
            throw new AiModelException("LLM 调用失败: " + e.getMessage(), e);
        }
    }

    private String resolveUserMessage(LlmChatRequest request) {
        if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
            return request.getUserMessage();
        }
        if (request.getMessages() != null) {
            for (int i = request.getMessages().size() - 1; i >= 0; i--) {
                LlmMessage m = request.getMessages().get(i);
                if (m != null && "user".equalsIgnoreCase(m.getRole()) && m.getContent() != null) {
                    return m.getContent();
                }
            }
        }
        return null;
    }

    @Override
    public String providerId() {
        return "deepseek";
    }

    @Override
    public String defaultModel() {
        return model;
    }
}
