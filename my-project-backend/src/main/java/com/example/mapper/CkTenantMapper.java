package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.Tenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/23 15:14
 */
@Mapper
public interface CkTenantMapper extends BaseMapper<Tenant> {
}
