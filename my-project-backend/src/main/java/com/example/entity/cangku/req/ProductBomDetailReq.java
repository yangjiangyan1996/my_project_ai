package com.example.entity.cangku.req;

import com.example.enums.CkProductEnums;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/6 01:03
 */
@Data
public class ProductBomDetailReq {
    private Long componentProductId;
    private Double lossRate;
    /**
     * {@link CkProductEnums.BomDetailType}
     */
    private Integer type;
    private Double quantity;
    //基础单位数量（多少成品用一个包装）
    private BigDecimal otherQuantity;
    private String remark;
    private Integer sortOrder;
}
