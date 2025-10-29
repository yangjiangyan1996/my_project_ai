package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.Inventory;
import org.apache.ibatis.annotations.Mapper;

// CkInventoryMapper.java
@Mapper
public interface CkInventoryMapper extends BaseMapper<Inventory> {
}