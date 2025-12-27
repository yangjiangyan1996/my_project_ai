package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/27 18:28
 */
@Data
public class StockDetailResp {
    private Long id;
    private String stockTakeNo;
    private String warehouseName;
    private String takeTypeName;
    private String approvalStatusName;
    private String takeStatusName;
    private String remark;
}
