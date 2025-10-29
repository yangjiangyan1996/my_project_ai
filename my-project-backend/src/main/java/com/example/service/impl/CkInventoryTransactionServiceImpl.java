package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InventoryTransaction;
import com.example.mapper.CkInventoryTransactionMapper;
import com.example.service.CkInventoryTransactionService;
import org.springframework.stereotype.Service;

// CkInventoryTransactionServiceImpl.java
@Service
public class CkInventoryTransactionServiceImpl extends ServiceImpl<CkInventoryTransactionMapper, InventoryTransaction> implements CkInventoryTransactionService {
    
}