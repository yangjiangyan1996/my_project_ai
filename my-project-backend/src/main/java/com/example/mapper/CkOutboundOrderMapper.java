package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;

// CkOutboundOrderMapper.java
@Mapper
public interface CkOutboundOrderMapper extends BaseMapper<OutboundOrder> {
}