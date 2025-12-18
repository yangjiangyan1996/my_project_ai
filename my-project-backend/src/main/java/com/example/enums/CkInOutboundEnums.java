package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 22:08
 */
public class CkInOutboundEnums {
    /**
     * 库存锁定类型枚举
     */
    @Getter
    public enum InventoryLockType {
        SALES_OUTBOUND(1, "销售出库"),
        PRODUCTION_OUTBOUND(2, "生产领料出库"),
        RETURN_OUTBOUND(3, "退货出库"),
        ALLOCATION_OUTBOUND(4, "调拨出库"),
        INVENTORY_CHECK(5, "盘点锁定"),
        OTHER(99, "其他");

        private final Integer code;
        private final String desc;

        InventoryLockType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 库存锁定状态枚举
     */
    @Getter
    public enum InventoryLockStatus {
        LOCKED(1, "锁定中"),
        PARTIALLY_UNLOCKED(2, "部分解锁"),
        FULLY_UNLOCKED(3, "全部解锁"),
        FORCE_RELEASED(4, "强制释放");

        private final Integer code;
        private final String desc;

        InventoryLockStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 锁定用途枚举
     */
    @Getter
    public enum LockPurpose {
        SALES_OCCUPY(1, "销售占用"),
        PRODUCTION_OCCUPY(2, "生产占用"),
        RETURN_OCCUPY(3, "退货占用"),
        ALLOCATION_OCCUPY(4, "调拨占用"),
        INVENTORY_FREEZE(5, "盘点冻结"),
        LOCATION_RESERVE(6, "仓位预留");

        private final Integer code;
        private final String desc;

        LockPurpose(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 锁定方向枚举
     */
    @Getter
    public enum LockDirection {
        OUTBOUND_LOCK(1, "出库锁定"),
        INBOUND_RESERVE(2, "入库预占");

        private final Integer code;
        private final String desc;

        LockDirection(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }


    //是否为推荐产品, 0=不是 1=推荐产品 2=包装件
    @Getter
    public enum IsRecommendProduct {
        // 0=不是
        No(0,"不是"),
        RecommendProduct(1,"推荐产品"),
        PackageProduct(2,"包装件");

        private Integer code;
        private String desc;
        IsRecommendProduct(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public static String getDescByCode(Integer code) {
            for (CkInOutboundEnums.IsRecommendProduct value : CkInOutboundEnums.IsRecommendProduct.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }


    //是否为触发产品, 0=是。1=不是
    @Getter
    public enum IsTriggerProduct {
        Yes(0,"是"),
        No(1,"不是");
        private Integer code;
        private String desc;
        IsTriggerProduct(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (CkInOutboundEnums.IsTriggerProduct value : CkInOutboundEnums.IsTriggerProduct.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }

    //状态:
    // 1-等待生产, (刚申请生产领料出库单，未审核)
    // 2-等待生产入库, (申请生产领料出库单，已审核)
    // 3-已生产完成,(申请生产领料出库单，已审核，并且数量已经生产入库)
    // 9-已取消，
    @Getter
    public enum ProductionTaskStatus {
        InProduction(1,"等待生产"),
        PartialCompletion(2,"等待生产入库"),
        Completed(3,"已生产完成"),

        //Cancelled(9,"已取消");
        ;
        ProductionTaskStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        private Integer code;
        private String desc;
        public static String getDescByCode(Integer code) {
            for (CkInOutboundEnums.ProductionTaskStatus value : CkInOutboundEnums.ProductionTaskStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }

    //产品类型（1=商品，2=原料）',
    @Getter
    public enum ProductType {
        Product(1,"商品"),
        RawMaterial(2,"原料");
        private Integer code;
        private String desc;
        ProductType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (CkInOutboundEnums.ProductType value : CkInOutboundEnums.ProductType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }

    //出库类型:1-销售出库,2-生产领料,3-退货出库,4-调拨出库
    @Getter
    public enum OutBoundType {
        SaleOutbound(1,"销售出库"),
        ProductionOutbound(2,"生产领料"),
        ReturnOutbound(3,"退货出库"),
        TransferOutbound(4,"调拨出库");
        private Integer code;
        private String desc;
        OutBoundType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static OutBoundType getByCode(Integer code) {
            for (CkInOutboundEnums.OutBoundType value : CkInOutboundEnums.OutBoundType.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    //入库类型:1-采购入库,2-生产入库,3-退货入库,4-调拨入库
    @Getter
    public enum InBoundType {
        PurchaseInbound(1,"采购入库"),
        ProductionInbound(2,"生产入库"),
        ReturnInbound(3,"退货入库"),
        TransferInbound(4,"调拨入库");
        private Integer code;
        private String desc;
        InBoundType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public static InBoundType getByCode(Integer code) {
            for (CkInOutboundEnums.InBoundType value : CkInOutboundEnums.InBoundType.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }
    //状态:0-待提交(草稿),1-审核中,2-审核已通过,3-入库已完成,4-已拒绝,9-已取消',
    @Getter
    public enum InOutBoundStatus {
        WaitSubmit(0,"待提交(草稿)"),
        WaitAudit(1,"审核中"),
        AuditPass(2,"审核已通过"),
        InOutboundComplete(3,"出入库已完成"),
        Reject(4,"已拒绝"),
        Cancel(9,"已取消");

        private Integer code;
        private String desc;
        InOutBoundStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (CkInOutboundEnums.InOutBoundStatus value : CkInOutboundEnums.InOutBoundStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
}
