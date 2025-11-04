package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.cangku.dto.OutboundOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 00:40
 */
@Mapper
public interface CkOutboundOrderItemMapper extends BaseMapper<OutboundOrderItem> {
}
