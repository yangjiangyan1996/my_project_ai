package com.example.config;

import java.util.Arrays;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/19 16:10
 */
public class Config {
    public static final String[] WHITE_URL = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verifyEmail",
            "/api/auth/sendVerifyEmail",
            "/api/public/**"
    };
}
