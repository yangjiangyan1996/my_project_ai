package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.WarehouseShelf;
import com.example.mapper.CkShelfMapper;
import com.example.service.CkShelfService;
import org.springframework.stereotype.Service;

// CkShelfServiceImpl.java
@Service
public class CkShelfServiceImpl extends ServiceImpl<CkShelfMapper, WarehouseShelf> implements CkShelfService {

}