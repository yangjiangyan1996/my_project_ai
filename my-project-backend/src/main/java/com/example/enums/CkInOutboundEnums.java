package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 22:08
 */
public class CkInOutboundEnums {
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
