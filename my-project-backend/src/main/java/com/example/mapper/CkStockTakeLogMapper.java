package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.StockTakeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/26 23:05
 */
@Mapper
public interface CkStockTakeLogMapper extends BaseMapper<StockTakeLog> {
}
