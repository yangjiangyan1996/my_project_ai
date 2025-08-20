package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Configuration
public class DeepSeekConfig {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.timeout:60}")
    private int timeoutSeconds;

    @Value("${deepseek.api.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;
    @Bean
    public WebClient deepSeekWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}