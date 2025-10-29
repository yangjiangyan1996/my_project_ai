package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InboundOrder;
import com.example.mapper.CkInboundOrderMapper;
import com.example.service.CkInboundOrderService;
import org.springframework.stereotype.Service;

// CkInboundOrderServiceImpl.java
@Service
public class CkInboundOrderServiceImpl extends ServiceImpl<CkInboundOrderMapper, InboundOrder> implements CkInboundOrderService {
    
}