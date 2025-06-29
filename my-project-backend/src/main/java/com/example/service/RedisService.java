package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.RedisDTO;

import java.util.concurrent.TimeUnit;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/28 16:33
 */
public interface RedisService extends IService<RedisDTO> {
    void saveValue(String key, String valueOf, int expire, TimeUnit minutes);

    String getValue(String key);

    void deleteByK(String s);
}
