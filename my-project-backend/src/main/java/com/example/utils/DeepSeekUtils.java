package com.example.utils;

import com.example.entity.deepseek.ChatRequest;
import com.example.entity.deepseek.DeepSeekResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class DeepSeekUtils {

    private final WebClient webClient;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.api.timeout:30}")
    private int timeoutSeconds;

    public DeepSeekUtils(WebClient webClient) {
        this.webClient = webClient;
    }

    public String callDeepSeek(String userMessage) {
        return callDeepSeek(userMessage, null);
    }

    public String callDeepSeek(String userMessage, String systemPrompt) {
        ChatRequest request = buildSimpleRequest(userMessage, systemPrompt);
        DeepSeekResponse response = chatCompletions(request);
        return extractContent(response);
    }

    /**
     * Full chat completions (supports tools / multi-turn). Returns raw provider response.
     */
    public DeepSeekResponse chatCompletions(ChatRequest request) {
        if (request == null) {
            throw new RuntimeException("DeepSeek ChatRequest 不能为空");
        }
        if (request.getModel() == null || request.getModel().isBlank()) {
            request.setModel(model);
        }
        long startTime = System.currentTimeMillis();
        try {
            DeepSeekResponse response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(DeepSeekResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            logDuration(startTime);
            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new RuntimeException("DeepSeek API 返回空响应");
            }
            return response;
        } catch (RuntimeException e) {
            log.error("调用 DeepSeek API 失败", e);
            throw e;
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            throw new RuntimeException("调用 DeepSeek API 失败: " + e.getMessage(), e);
        }
    }

    public String getDefaultModel() {
        return model;
    }

    private ChatRequest buildSimpleRequest(String userMessage, String systemPrompt) {
        ChatRequest request = new ChatRequest();
        request.setModel(model);
        request.setTemperature(0.7);
        request.setMax_tokens(2000);

        ChatRequest.Message userMsg = new ChatRequest.Message("user", userMessage);
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            ChatRequest.Message systemMsg = new ChatRequest.Message("system", systemPrompt);
            request.setMessages(List.of(systemMsg, userMsg));
        } else {
            request.setMessages(List.of(userMsg));
        }
        return request;
    }

    private String extractContent(DeepSeekResponse response) {
        String result = response.getChoices().get(0).getMessage().getContent();
        return result == null ? "" : result;
    }

    private void logDuration(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("DeepSeek API 调用成功，耗时: {}ms", duration);
    }
}
