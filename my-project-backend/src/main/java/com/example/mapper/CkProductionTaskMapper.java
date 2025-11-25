package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.ProductionTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/24 23:18
 */
@Mapper
public interface CkProductionTaskMapper extends BaseMapper<ProductionTask> {
}
