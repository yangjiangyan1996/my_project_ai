package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.QuanUserBarFollows;
import com.example.entity.req.BarMyFavoriteReq;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/24 16:00
 */
public interface QuanUserBarFollowsService extends IService<QuanUserBarFollows> {
    QuanUserBarFollows selectByBarIdAndUserId(Long barId, Long userId);

    Boolean insert(Long barId, Long userId);

    Long selectCountByBarId(Long barId);

    Page<QuanUserBarFollows> getMyFavoriteBar(Page<QuanUserBarFollows> of, BarMyFavoriteReq req);
}
