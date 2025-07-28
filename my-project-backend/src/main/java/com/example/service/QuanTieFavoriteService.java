package com.example.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.entity.dto.QuanTieFavorite;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/28 17:12
 */
public interface QuanTieFavoriteService {
    Boolean insertOne(QuanTieFavorite f);

    Boolean removeOne(Long tieId, Long userId);

    boolean selectByTieIdAndUserId(Long tieId, Long userId);

    List<QuanTieFavorite> selectByTieIds(List<Long> tieIds);
    List<QuanTieFavorite> selectByTieId(Long tieId);
}
