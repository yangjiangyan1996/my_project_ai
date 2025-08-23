package com.example.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/23 09:33
 */
@Configuration
@Getter
public class TenXunConfig {

    @Value("${tengxun.secretId}")
    public String secretId;
    @Value("${tengxun.secretKey}")
    public String secretKey;
}
