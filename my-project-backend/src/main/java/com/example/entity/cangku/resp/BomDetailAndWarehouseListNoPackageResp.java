package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * String @Date 2025/11/6 00;
 */
@Data
public class BomDetailAndWarehouseListNoPackageResp {
    //bomdetail的ID
    private Long id;
    Long componentProductId;
    String componentProductName;
    String componentProductSku;
    String componentProductSpec;
    String componentProductColor;
    String componentProductUnit;
    BigDecimal quantity;
    //相同原料合并的详情
    List<UsageDetail> usageDetailList;

    List<ProductWarehouseQuantityResp> warehouseQuantityList;


    /**
     * bomdetail的合并相同原料的用量信息
     */
    @Data
    public static class UsageDetail {
        private Long bomDetailId;
        private BigDecimal quantity;
        private Integer type;
        private BigDecimal lossRate;
        private String remark;
        private Integer sortOrder;
    }
}
