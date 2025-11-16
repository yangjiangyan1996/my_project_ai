package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 22:08
 */
public class CkInOutboundEnums {

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
