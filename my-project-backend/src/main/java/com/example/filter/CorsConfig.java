package com.example.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 匹配所有路径
                .allowedOrigins("*")  // 允许所有来源（生产环境应替换为具体域名）
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS", "TRACE", "PATCH")  // 允许所有方法
                .allowedHeaders("Authorization", "Content-Type")  // 允许的请求头
                .allowCredentials(false)  // 不允许凭据（与您的配置一致）
                .maxAge(3600);  // 预检请求缓存时间（秒）
    }
}