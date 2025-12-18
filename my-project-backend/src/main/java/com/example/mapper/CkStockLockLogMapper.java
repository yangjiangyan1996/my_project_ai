package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.StockLockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/17 23:07
 */
@Mapper
public interface CkStockLockLogMapper extends BaseMapper<StockLockLog> {
}
