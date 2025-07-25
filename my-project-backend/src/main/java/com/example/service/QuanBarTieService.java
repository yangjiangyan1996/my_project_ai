package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.QuanBarTie;
import com.example.entity.req.QuanTieListPageReq;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 14:35
 */
public interface QuanBarTieService extends IService<QuanBarTie> {
    Page<QuanBarTie> getTiePageOfBar(Page<QuanBarTie> of, QuanTieListPageReq req);
}
