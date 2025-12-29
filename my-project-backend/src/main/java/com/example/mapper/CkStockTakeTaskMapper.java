package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.StockTakeTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/28 12:52
 */
@Mapper
public interface CkStockTakeTaskMapper  extends BaseMapper<StockTakeTask> {
}
