package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Inventory;
import com.example.mapper.CkInventoryMapper;
import com.example.service.CkInventoryService;
import org.springframework.stereotype.Service;

// CkInventoryServiceImpl.java
@Service
public class CkInventoryServiceImpl extends ServiceImpl<CkInventoryMapper, Inventory> implements CkInventoryService {

}