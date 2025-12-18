package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockLockLog;
import com.example.mapper.CkStockLockLogMapper;
import com.example.service.CkStockLockLogService;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/17 23:07
 */
@Service
public class CkStockLockLogServiceImpl  extends ServiceImpl<CkStockLockLogMapper, StockLockLog> implements CkStockLockLogService {
}
