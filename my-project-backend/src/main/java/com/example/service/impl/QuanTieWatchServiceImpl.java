package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.QuanTieWatch;
import com.example.mapper.QuanTieWatchMapper;
import com.example.service.QuanTieWatchService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:29
 */
@Service
public class QuanTieWatchServiceImpl  extends ServiceImpl<QuanTieWatchMapper, QuanTieWatch> implements QuanTieWatchService {
    @Override
    public void insert(Long tieId, Long userId) {
        QuanTieWatch w = new QuanTieWatch();
        w.setTieId(tieId);
        w.setUserId(userId);
        w.setCreatedAt(new Date());
        w.setCreatedBy(userId);
        w.setModifiedAt(new Date());
        w.setModifiedBy(userId);
        w.setIsDeleted(0);
        this.baseMapper.insert(w);
    }
}
