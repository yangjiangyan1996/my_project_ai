package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.CustomerSkuMapping;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/16 23:13
 */
@Mapper
public interface CkCustomerSkuMappingMapper  extends BaseMapper<CustomerSkuMapping> {
}
