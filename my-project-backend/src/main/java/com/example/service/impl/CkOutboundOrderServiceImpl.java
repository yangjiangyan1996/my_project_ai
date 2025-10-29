package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.OutboundOrder;
import com.example.mapper.CkOutboundOrderMapper;
import com.example.service.CkOutboundOrderService;
import org.springframework.stereotype.Service;

// CkOutboundOrderServiceImpl.java
@Service
public class CkOutboundOrderServiceImpl extends ServiceImpl<CkOutboundOrderMapper, OutboundOrder> implements CkOutboundOrderService {

}