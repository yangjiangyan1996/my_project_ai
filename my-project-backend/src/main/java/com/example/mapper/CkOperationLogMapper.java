package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CkOperationLogMapper extends BaseMapper<OperationLog> {
}