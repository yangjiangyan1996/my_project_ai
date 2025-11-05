package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

//供应商表
@Data
public class InventoryListResp {
    private Long id;

    //产品ID
    private Long productId;

    //产品名称
    private String productName;

    //规格型号
    private String spec;

    //颜色
    private String color;

    //单位ID
    private String unitName;

    //仓库的存储数量细节
    //List<WarehouseInventory> warehouseInventoryList;

    //出库数量细节
    //List<WarehouseInventory> outboundQuantityList;

    //总进货数量
    //private BigDecimal totalInQuantityOfAllWarehouses;
    //总出货数量
    //private BigDecimal totalOutboundQuantityOfAllWarehouses;
    //可用库存
    private BigDecimal availableQuantity;
    //总库存
    private BigDecimal quantity;
    //锁定库存
    private BigDecimal lockedQuantity;

    //出货单位（比如按箱子出货
    private String outUnitName;

    //出货单位数量，比如出货时一箱子多少货
    private BigDecimal outUnitPerNum;

    //出货单位数量的总数量
    //private BigDecimal outUnitTotalNum;

    //体积
    //private BigDecimal volume;

    //单件重量
    //private BigDecimal weightPerUnit;

    //总重量
    //private BigDecimal weightAll;

    //条形码
    //private String barcode;

    //sku
    private String sku;

    //单价（人民币）
    private BigDecimal priceRmb;

    //总价（人民币）
    private BigDecimal totalPriceRmb;

    //分类名称
    private String categoryName;

    /**
     * @link com.example.enums.CkInventoryEnums.InventoryStatus
     */
    private Integer inventoryStatus;

    @Data
    public static class WarehouseInventory {
        //仓库ID
        private Long warehouseId;
        //仓库名称
        private String warehouseName;
        //库存数量
        private BigDecimal quantity;
        // 可用数量 = quantity - lockedQuantity
        private BigDecimal availableQuantity;
        //锁定数量
        private BigDecimal lockedQuantity;
    }
}