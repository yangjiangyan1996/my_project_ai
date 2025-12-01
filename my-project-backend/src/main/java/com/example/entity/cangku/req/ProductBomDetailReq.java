package com.example.entity.cangku.req;

import com.example.enums.CkProductEnums;
import lombok.Data;

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
    private String remark;
    private Integer sortOrder;
}
