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
        long startTime = System.currentTimeMillis();

        ChatRequest request = buildRequest(userMessage, systemPrompt);

        try {
            DeepSeekResponse response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(DeepSeekResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            return processResponse(response, startTime);
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            throw new RuntimeException("调用 DeepSeek API 失败: " + e.getMessage(), e);
        }
    }

    private ChatRequest buildRequest(String userMessage, String systemPrompt) {
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

    private String processResponse(DeepSeekResponse response, long startTime) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new RuntimeException("DeepSeek API 返回空响应");
        }

        String result = response.getChoices().get(0).getMessage().getContent();
        logDuration(startTime);
        return result;
    }

    private void logDuration(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("DeepSeek API 调用成功，耗时: {}ms", duration);
    }
}