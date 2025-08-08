package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置类
 * 用于配置静态资源的访问路径和映射
 */
@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置图片资源的访问路径
        registry.addResourceHandler("/imgs/**")
                .addResourceLocations("classpath:/resources/imgs/");
    }
}