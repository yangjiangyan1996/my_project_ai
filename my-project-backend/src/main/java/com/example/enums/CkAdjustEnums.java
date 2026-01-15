package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2026/1/14 18:20
 */
public class CkAdjustEnums {

    @Getter
    public enum AdjustOrderStatus {
        //调整状态\n状态:0-待提交(草稿),1-审核中,2-审核已通过,4-已拒绝,11-开始执行，9-已取消\n20-调整完成 29-调整失败
        WaitSubmit(0,"待提交(草稿)"),
        WaitAudit(1,"审核中"),
        AuditPass(2,"审核已通过"),
        Reject(4,"已拒绝"),
        Cancel(9,"已取消"),
        StartExecute(11,"开始执行"),
        AdjustComplete(20,"调整完成"),
        AdjustFail(29,"调整失败");
        ;

        private String name;
        private Integer code;

        AdjustOrderStatus(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getNameByCode(Integer code) {
            for (AdjustOrderStatus value : AdjustOrderStatus.values()) {
                if (value.code.equals(code)) {
                    return value.name;
                }
            }
            return null;
        }
    }


    @Getter
    public enum AdjustOrderItemStatus {
        WaitExecute(1,"待执行"),
        Executed(2,"已执行"),
        Canceled(3,"已取消");
        private String name;
        private Integer code;
        AdjustOrderItemStatus(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
        public static String getNameByCode(Integer code) {
            for (AdjustOrderItemStatus value : AdjustOrderItemStatus.values()) {
                if (value.code.equals(code)) {
                    return value.name;
                }
            }
            return null;
        }
    }
}
