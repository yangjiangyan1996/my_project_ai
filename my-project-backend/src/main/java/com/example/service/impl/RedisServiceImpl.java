package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.RedisDTO;
import com.example.mapper.RedisMapper;
import com.example.service.RedisService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/28 16:33
 */
@Service
public class RedisServiceImpl extends ServiceImpl<RedisMapper, RedisDTO> implements RedisService {
    @Override
    public void saveValue(String key, String valueOf, int expire, TimeUnit minutes) {
        RedisDTO r = new RedisDTO();
        r.setK( key);
        r.setV(valueOf);
        // 计算过期时间（当前时间 + expire × timeUnit）
        Date expireTime = calculateExpireTime(expire, minutes);
        r.setE(expireTime);
        r.setCreatedAt(new Date());
        r.setModifiedAt(new Date());
        r.setCreatedBy(999L);
        r.setModifiedBy(999L);

        //删除k=key 的所有数据
        remove(new QueryWrapper<RedisDTO>().eq("k", key));
        save(r);
    }

    @Override
    public String getValue(String key) {
        RedisDTO key1 = this.baseMapper.selectOne(new QueryWrapper<RedisDTO>().eq("k", key).eq("is_deleted", 0));
        if (key1 == null) {
            return null;
        }
        if (key1.getE().before(new Date())) {
            removebykey(key);
            return null;
        }
        return key1.getV();
    }

    private void removebykey(String key) {
        this.remove(new QueryWrapper<RedisDTO>().eq("k", key));
    }

    @Override
    public void deleteByK(String s) {
        this.remove(new QueryWrapper<RedisDTO>().eq("k", s));
    }

    /**
     * 计算过期时间
     * @param expire 过期时间量
     * @param timeUnit 时间单位
     * @return 计算后的过期时间
     */
    private Date calculateExpireTime(int expire, TimeUnit timeUnit) {
        // 获取当前时间
        Date now = new Date();

        // 根据时间单位转换为毫秒
        long expireMillis = timeUnit.toMillis(expire);

        // 计算过期时间
        return new Date(now.getTime() + expireMillis);
    }
}
