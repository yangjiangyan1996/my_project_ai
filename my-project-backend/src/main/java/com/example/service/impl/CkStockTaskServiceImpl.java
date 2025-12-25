package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockTask;
import com.example.mapper.CkStockTaskMapper;
import com.example.service.CkStockTaskService;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:53
 */
@Service
public class CkStockTaskServiceImpl extends ServiceImpl<CkStockTaskMapper, StockTask> implements CkStockTaskService {
}
