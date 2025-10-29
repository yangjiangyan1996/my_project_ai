package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockTake;
import com.example.mapper.CkStockTakeMapper;
import com.example.service.CkStockTakeService;
import org.springframework.stereotype.Service;

// CkStockTakeServiceImpl.java
@Service
public class CkStockTakeServiceImpl extends ServiceImpl<CkStockTakeMapper, StockTake> implements CkStockTakeService {

}