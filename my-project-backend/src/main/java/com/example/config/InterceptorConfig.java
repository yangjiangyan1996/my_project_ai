//package com.example.config;
//
//import com.example.aop.task.LogInterceptor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Slf4j
//@Configuration
//public class InterceptorConfig implements WebMvcConfigurer {
//
//    private final LogInterceptor logInterceptor;
//
//    public InterceptorConfig(LogInterceptor logInterceptor) {
//        this.logInterceptor = logInterceptor;
//        log.info("InterceptorConfig 初始化");
//    }
//
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        log.info("========== 注册 LogInterceptor ==========");
//
//        // 只拦截 Controller 方法，由拦截器内部判断是否有 @LogOperation 注解
//        registry.addInterceptor(logInterceptor)
//                .addPathPatterns("/api/**")  // 拦截所有 API
//                .excludePathPatterns(
//                        "/swagger-ui/**",
//                        "/v3/api-docs/**",
//                        "/webjars/**",
//                        "/error",
//                        "/favicon.ico",
//                        "/imgs/**"
//                );
//
//        log.info("LogInterceptor 注册完成，将只记录带有 @LogOperation 注解的请求");
//    }
//}