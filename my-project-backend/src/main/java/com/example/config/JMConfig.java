package com.example.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/25 10:56
 */
@Getter
@Configuration
public class JMConfig {
    @Value("${jimeng.accessKeyId}")
    private String accessKey;
    @Value("${jimeng.accessKeySecret}")
    private String accessKeySecret;
}
