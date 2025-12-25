package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.StockTaskSummary;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian    // 作者名称
 * @Description       // 接口描述信息（此处为空）
 * @Email 1776080295@qq.com  // 作者邮箱
 * @Date 2025/12/25 11:51    // 创建日期
 */
@Mapper
public interface CkStockTaskSummaryMapper extends BaseMapper<StockTaskSummary> {
}
