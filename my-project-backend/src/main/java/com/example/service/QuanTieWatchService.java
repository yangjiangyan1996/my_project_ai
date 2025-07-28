package com.example.service;

import com.example.entity.dto.QuanTieWatch;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:28
 */
public interface QuanTieWatchService {
    void insert(Long tieId, Long userId);

    List<QuanTieWatch> selectByTieIds(List<Long> tieIds);
    List<QuanTieWatch> selectByTieId(Long tieId);
}
