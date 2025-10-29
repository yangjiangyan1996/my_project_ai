package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.InventoryTransaction;
import org.apache.ibatis.annotations.Mapper;

// CkInventoryTransactionMapper.java
@Mapper
public interface CkInventoryTransactionMapper extends BaseMapper<InventoryTransaction> {
}