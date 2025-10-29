package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.StockTake;
import org.apache.ibatis.annotations.Mapper;

// CkStockTakeMapper.java
@Mapper
public interface CkStockTakeMapper extends BaseMapper<StockTake> {
}