package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/27 00:12
 */
@Data
public class BomDetailListOfProductResp {
    private Long bomDetailId;

    //原料产品SKU
    private String componentProductSku;

    //原料产品ID（对应 ck_product.id）
    private Long componentProductId;

    //原料产品名称
    private String componentProductName;

    //原料规格
    private String componentProductSpec;

    //原料颜色
    private String componentProductColor;

    //生产一个成品所需的原料数量
    private BigDecimal quantity;

    //基础单位数量（多少成品用一个包装）
    private BigDecimal otherQuantity;

    //损耗率（百分比）
    private String lossRate;

    //生产一个成品所需的原料净用量数量（目前只存储，无逻辑）
    private BigDecimal quantityBeforeLoss;

    //备注，比如原料替代说明
    private String remark;

    //顺序
    private Integer sortOrder;

    //1=主料，2=辅料,
    private Integer type;

    /**
     * {@link com.example.enums.CkProductEnums.BomDetailType}
     */
    private String typeName;
}
