package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.InboundOrder;
import org.apache.ibatis.annotations.Mapper;

// CkInboundOrderMapper.java
@Mapper
public interface CkInboundOrderMapper extends BaseMapper<InboundOrder> {
}