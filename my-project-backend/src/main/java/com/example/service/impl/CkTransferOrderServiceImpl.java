package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.TransferOrder;
import com.example.mapper.CkTransferOrderMapper;
import com.example.service.CkTransferOrderService;
import org.springframework.stereotype.Service;

// CkTransferOrderServiceImpl.java
@Service
public class CkTransferOrderServiceImpl extends ServiceImpl<CkTransferOrderMapper, TransferOrder> implements CkTransferOrderService {

}