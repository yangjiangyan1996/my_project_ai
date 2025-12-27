package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.StockTakeLock;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/26 23:04
 */
@Mapper
public interface CkStockTakeLockMapper extends BaseMapper<StockTakeLock> {
}
